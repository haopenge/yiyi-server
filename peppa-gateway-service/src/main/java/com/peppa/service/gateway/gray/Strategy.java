/*    */ package com.peppa.service.gateway.gray;
/*    */ 
/*    */ import com.netflix.loadbalancer.ILoadBalancer;
/*    */ import com.netflix.loadbalancer.Server;
/*    */ import org.springframework.http.server.reactive.ServerHttpRequest;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public interface Strategy
/*    */   extends Comparable<Strategy>
/*    */ {
/*    */   public static final String HTTP_X_FORWARDED_FOR = "x-forwarded-for";
/*    */   public static final String HUOHUA_PREFIX = "huohua-";
/*    */   public static final String A_IP = "A_IP_";
/*    */   public static final String A_DEV = "A_DEV_";
/*    */   public static final String S_IP = "S_REGIP_";
/*    */   public static final String S_NA = "S_REGNA_";
/*    */   public static final String S_TAG = "S_TAG_";
/*    */   public static final String H_SVIP = "H_SVIP_";
/*    */   public static final String H_IPS = "H_IPS_";
/*    */   public static final String TAG_WHITELIST = "graywl";
/*    */   public static final String TAG_BLACKLIST = "graybl";
/*    */   public static final String DEFAULT_STRATEGY = "DEF";
/*    */   public static final int SORT_H_IPS = 1;
/*    */   public static final int SORT_H_SVIP = 2;
/*    */   public static final int SORT_S_TAG = 3;
/*    */   public static final int SORT_S_IP = 4;
/*    */   public static final int SORT_S_NA = 5;
/*    */   public static final int SORT_A_IP = 6;
/*    */   public static final int SORT_A_DEV = 7;
/*    */   public static final String DELIM_ITEM = ",";
/*    */   public static final String DELIM_VALUE = ":";
/*    */   
/*    */   String getName();
/*    */   
/*    */   Server getServer(ILoadBalancer paramILoadBalancer, ServerHttpRequest paramServerHttpRequest);
/*    */   
/*    */   int getOrder();
/*    */   
/*    */   static String getHeaderValue(String header_key, ServerHttpRequest request) {
/* 50 */     String headervalue = request.getHeaders().getFirst("huohua-".concat(header_key));
/* 51 */     if (headervalue == null || headervalue.trim().equals("")) {
/* 52 */       return null;
/*    */     }
/* 54 */     return headervalue.trim();
/*    */   }
/*    */ }


/* Location:              /Users/liupenghao/my-app/source/peppa-service-gateway-0.0.3-20210909.123447-9.jar!/com/peppa/service/gateway/gray/Strategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */