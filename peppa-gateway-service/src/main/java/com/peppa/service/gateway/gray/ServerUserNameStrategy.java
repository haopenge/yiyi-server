/*    */ package com.peppa.service.gateway.gray;
/*    */ 
/*    */ import com.netflix.loadbalancer.ILoadBalancer;
/*    */ import com.netflix.loadbalancer.Server;
/*    */ import com.peppa.service.gateway.gray.factory.AnnoStrategy;
/*    */ import org.springframework.http.server.reactive.ServerHttpRequest;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @AnnoStrategy
/*    */ public class ServerUserNameStrategy
/*    */   extends StrategyAbstract
/*    */ {
/* 15 */   private final String header_key = "uname";
/* 16 */   private final String strategy_key = "gray-name";
/*    */   
/*    */   public ServerUserNameStrategy() {
/* 19 */     setName("S_REGNA_");
/* 20 */     setOrder(5);
/*    */   }
/*    */ 
/*    */   
/*    */   public Server getServer(ILoadBalancer balancer, ServerHttpRequest request) {
/* 25 */     return getServerByHeader(balancer, "uname", "gray-name", request);
/*    */   }
/*    */ 
/*    */   
/*    */   public String getName() {
/* 30 */     return this.name;
/*    */   }
/*    */ }


/* Location:              /Users/liupenghao/my-app/source/peppa-service-gateway-0.0.3-20210909.123447-9.jar!/com/peppa/service/gateway/gray/ServerUserNameStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */