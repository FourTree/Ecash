package cn.z.ecash.commn;


/**
 * TLV Node Define
 * @author wangyun
 *
 */
public class TLVNode {
        /**
         * TLV TAG
         */
        protected int tag = 0;
        
        /**
         * TLV LENGTH
         */
        public int length = 0;
        
        /**
         * TLV VALUE
         */
        protected byte [] value;
}