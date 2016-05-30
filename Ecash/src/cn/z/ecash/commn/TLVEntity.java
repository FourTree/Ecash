package cn.z.ecash.commn;

import java.util.ArrayList;

// import android.util.Log;

/**
 * TLV Entity
 * @author Administrator
 *
 */
public class TLVEntity extends TLVNode  {
	/**
	 * child tag nodes
	 */
	private ArrayList<TLVEntity> nodes = new ArrayList<TLVEntity>();
	
	/**
	 * single node, nodes count is zero.
	 */
	private boolean singleNode = false;
	
	/**
	 * PARSER ERROR CODE
	 */
	private int error_code = 0;
	
	/**
	 * 是否是当一节点
	 * @return
	 */
	public boolean isSingleNode() {
		return singleNode;
	}
	
	/**
	 * 构造TLV实体
	 * @param tag
	 */
	public TLVEntity(int tag,boolean singleNode) {
		this.tag = tag;
		this.singleNode = singleNode;
		this.error_code = 0;
	}
	
	public boolean isParserError() {
		if (error_code != 0) return true;
		if (!this.singleNode) {
			for (int i =0; i < nodes.size(); i++) {
				if (nodes.get(i).isParserError()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public byte [] getValue() {
		return this.value;
	}
	
	public int getTag() {
		return this.tag;
	}
	
	private final static String TAG = "TLV-Parser";
	
	public void DebugOutput() {
		StringBuilder debugText = new StringBuilder();
		
//		Log.d(TAG, "=== TLV-Parser Debug ===");
		System.out.println("=== TLV-Parser Debug ===");
		
		if (this.singleNode) {
			debugText.append("[T]");
			debugText.append(Util.intToHexString(this.tag));
			debugText.append(",[V]");
			debugText.append(Util.toHexString(this.value, 0,this.length));
			debugText.append(",("+TAGInformation.getTagInformation(this.tag) + ")");
//			Log.d(TAG, debugText.toString());
			System.out.println(debugText.toString());
		} else {
			debugText.append("[T]");
			debugText.append(Util.intToHexString(this.tag));
			debugText.append(",("+TAGInformation.getTagInformation(this.tag) + ")");
			
//			Log.d(TAG, debugText.toString());
			System.out.println(debugText.toString());
			
			for (int i =0; i < nodes.size(); i++) {
				nodes.get(i).DebugOutputDep(1);
			}
		}
		
//		Log.d(TAG, "=== TLV-Parser Debug END ===");
		System.out.println("=== TLV-Parser Debug END ===");
	}
	
	private void DebugOutputDep(int dep) {
		
		StringBuilder debugText = new StringBuilder();
		String depTap = "-------------------------------";
		depTap = depTap.substring(0,dep);
		
		if (this.singleNode) {
			debugText.append(depTap);
			debugText.append("[T]");
			debugText.append(Util.intToHexString(this.tag));
			debugText.append(",[V]");
			debugText.append(Util.toHexString(this.value, 0,this.length));
			debugText.append(",("+TAGInformation.getTagInformation(this.tag) + ")");
			
//			Log.d(TAG, debugText.toString());
			System.out.println(debugText.toString());
			
		} else {
			debugText.append(depTap);
			debugText.append("[T]");
			debugText.append(Util.intToHexString(this.tag));
			debugText.append(",("+TAGInformation.getTagInformation(this.tag) + ")");
			
//			Log.d(TAG, debugText.toString());
			System.out.println(debugText.toString());
			
			for (int i =0; i < nodes.size(); i++) {
				nodes.get(i).DebugOutputDep(dep+1);
			}
		}
	}
	
	public ArrayList<TLVEntity> getNodes() {
		return this.nodes;
	}
	
	/**
	 * 通过Tag查找tag
	 * @param tag
	 * @return
	 */
	public TLVEntity findFirstEntityByTag(int tag) {
		TLVEntity retEntity = null;
		
		if (this.tag == tag) return this;
		
		if (!this.singleNode) {
			for (int i =0; i < nodes.size(); i++) {
				retEntity = nodes.get(i).findFirstEntityByTag(tag);
				if (retEntity != null) return retEntity;
			}
		}
		
		return null;
	}
		
	/**
	 * 设置Tag VALUE and Length
	 * 如果是复合结构需要解析后续内容
	 * 
	 * @param buffer
	 * @param offset
	 * @param length
	 */
	public void setTagValue(byte [] buffer, int offset, int length) {
		this.length = length;
		
		if(this.singleNode) {
			if (this.length>0) {
				this.value = new byte[this.length];
				System.arraycopy(buffer, offset, this.value, 0, this.length);
			}
			return;
		}
		
		// 同样存储，后续需要取相关数据
		// 2012-07-06 by wangyun
		if (this.length>0) {
			this.value = new byte[this.length];
			System.arraycopy(buffer, offset, this.value, 0, this.length);
		}
		
		// 复合结构，需要解析子节点内容
		if (!this.singleNode) {
			TLVParser paserContext = new TLVParser();
			TLVEntity entity = null;
			int tempOffset = 0;
			
			for (;;) {
				paserContext.setParserFlag(0);
				paserContext.setParserLen(0);
				
				entity = unpacket_entity(paserContext,buffer, offset + tempOffset, length - tempOffset);
				
				if (entity == null) {
//					Log.d(TAG,"unpacket_entity err.");
					System.out.println("unpacket_entity err.");
					
					error_code = -1;
					break;
				}
				
				nodes.add(entity);
				
				if (paserContext.getParserFlag() == 0) {
					break;
				} else if (paserContext.getParserFlag() == 1) {
					tempOffset += paserContext.getParserLen();
				} else {
					// 解析存在问题
//					Log.d(TAG,"unpacket_entity err. parser_flag:"+ paserContext.getParserFlag());

					System.out.println("unpacket_entity err. parser_flag:");
					System.out.println(paserContext.getParserFlag());

					
					error_code = -2;
					break;
				}
			}
		}
	}
	
	static int tlv_length_count(byte v) {
		int val = (v & 0xFF);
		
		if ( (val & 0x80) == 0x80) {
			return (val & 0x7F);
		}
		
		return 0;
	}
	
	public static ArrayList<TLVEntity> unpacket_entity(byte [] buffer, int offset, int length) throws TLVParserException {
		TLVEntity entity = new TLVEntity(0xFFFF,false);
		
		entity.setTagValue(buffer, offset, length);
		
		if (entity.isParserError()) {
//			Log.d(TAG, "parser tlv error.");
			System.out.println("parser tlv error.");
			throw new TLVParserException("parser tlv error.");
		}
		
		return entity.getNodes();
	}
	
	/**
	 * 解析TLV结构
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 */
	private static TLVEntity unpacket_entity(TLVParser parser, byte [] buffer, int _offset, int _length) {
		
		int tagL = 0;
		int tagH = 0;
		int tagCL= 0;
		
		int offset = _offset;
		
		TLVEntity entity = null;
		
		// Log.d(TAG, "Parser : "+ Util.toHexString(buffer, _offset, _length));
		tagH = buffer[offset++] & 0xFF;
		
		// 单一结构   
		if ( (tagH&0x20) != 0x20) {
			// tag两字节  
			if ( (tagH&0x1f) == 0x1f) {
				tagL = buffer[offset++] & 0xFF;
				tagCL = 0;
				
				entity = new TLVEntity(tagH<<8 | tagL, true);
				
				int length_count = tlv_length_count(buffer[offset]);
				
				if (length_count == 0) {
					tagCL = (buffer[offset] & 0xFF); offset ++;
				} else {
					offset ++;
					
					while(length_count>0) {
						tagCL *= 256;
						tagCL += (buffer[offset] & 0xFF); offset ++;
						
						length_count--;
					}
				}
				
				entity.setTagValue(buffer, offset, tagCL); offset += tagCL;
			}  
			else //tag单字节  
			{  
				tagCL = 0;
				entity = new TLVEntity(tagH,true);
				
				int length_count = tlv_length_count(buffer[offset]);
				
				if (length_count == 0) {
					tagCL = (buffer[offset] & 0xFF); offset ++;
				} else {
					offset ++;
					
					while(length_count>0) {
						tagCL *= 256;
						tagCL += (buffer[offset] & 0xFF); offset ++;
						
						length_count--;
					}
				}
				
				entity.setTagValue(buffer, offset, tagCL); offset += tagCL;
			}
			
			// 设置后续解析标志
			if (offset < (_offset + _length)) {
				// 还存在实体结构，可以继续解析
				if (_length - (offset - _offset) >= 3) {
					// Log.d(TAG, "parser continue : " + (_length - (offset - _offset)));
					parser.setParserFlag(1);
				} else {
					// Log.d(TAG, "parser length-offset : " + (_length - (offset - _offset)));
					parser.setParserFlag(2);
				}
			} else parser.setParserFlag(0);
			
			// 该实体结构占用的长度
			parser.setParserLen(offset-_offset);
			
			// 构件返回一个实体
			return entity;
		}
	                
		// 复合结构  
		if ( (tagH&0x20) == 0x20) {
			// tag两字节  
			if ( (tagH&0x1f) == 0x1f) {
				tagL = buffer[offset++] & 0xFF;
				tagCL = 0;
				
				entity = new TLVEntity(tagH<<8 | tagL, false);
				
				int length_count = tlv_length_count(buffer[offset]);
				
				if (length_count == 0) {
					tagCL = (buffer[offset] & 0xFF); offset ++;
				} else {
					offset ++;
					
					while(length_count>0) {
						tagCL *= 256;
						tagCL += (buffer[offset] & 0xFF); offset ++;
						
						length_count--;
					}
				}
				
				entity.setTagValue(buffer, offset, tagCL); offset += tagCL;
			}  
			else //tag单字节  
			{  
				tagCL = 0;
				entity = new TLVEntity(tagH,false);
				
				int length_count = tlv_length_count(buffer[offset]);
				
				if (length_count == 0) {
					tagCL = (buffer[offset] & 0xFF); offset ++;
				} else {
					offset ++;
					
					while(length_count>0) {
						tagCL *= 256;
						tagCL += (buffer[offset] & 0xFF); offset ++;
						
						length_count--;
					}
				}
				
				entity.setTagValue(buffer, offset, tagCL); offset += tagCL;
			}
			
			// 设置后续解析标志
			if (offset < (_offset + _length)) {
				// 还存在实体结构，可以继续解析
				if (_length - (offset - _offset) >= 3) {
					// Log.d(TAG, "parser continue : " + (_length - (offset - _offset)));
					parser.setParserFlag(1);
				} else {
					// Log.d(TAG, "parser exit break : " + (_length - (offset - _offset)));
					parser.setParserFlag(2);
				}
			} else parser.setParserFlag(0);
			
			// 该实体结构占用的长度
			parser.setParserLen(offset-_offset);
			
			// 构件返回一个实体
			return entity;
		}
		
		return null;
	}
	
	/**
	 * TLV解析辅助类
	 * 
	 * @author wangyun
	 *
	 */
	class TLVParser {
		/**
		 * 解析结构标志0: 没有后续节点，1：存在后续节点解析 2：后续解吸长度不正确
		 */
		private int parser_flag = 0;
		
		/**
		 * 该结构占用的空间
		 */
		private int parser_length = 0;
		
		public void setParserFlag(int flag) {
			this.parser_flag = flag;
		}
		
		public int getParserFlag() {
			return this.parser_flag;
		}
		
		public void setParserLen(int len) {
			this.parser_length = len;
		}
		
		public int getParserLen() {
			return this.parser_length;
		}
	}
}