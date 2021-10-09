/*    */ package com.yiyi.service.gateway.gray;
/*    */ 
/*    */ import com.netflix.loadbalancer.ILoadBalancer;
/*    */ import com.netflix.loadbalancer.Server;
/*    */ import com.yiyi.service.gateway.gray.factory.AnnoStrategy;
/*    */ import com.yiyi.service.gateway.utils.ReactiveRequestUtils;
/*    */ import org.springframework.http.server.reactive.ServerHttpRequest;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @AnnoStrategy
/*    */ public class ServerIpStrategy
/*    */   extends StrategyAbstract
/*    */ {
/* 16 */   private final String strategy_key = "gray-ip";
/*    */   
/*    */   public ServerIpStrategy() {
/* 19 */     setName("S_REGIP_");
/* 20 */     setOrder(4);
/*    */   }
/*    */ 
/*    */   
/*    */   public Server getServer(ILoadBalancer balancer, ServerHttpRequest request) {
/* 25 */     String ip = ReactiveRequestUtils.getRealIp(request);
/* 26 */     if (ip == null || ip.equals("")) {
/* 27 */       this.logger.info("HTTP_X_FORWARDED_FOR threadlocal ip:---null!!!");
/* 28 */       return null;
/*    */     } 
/* 30 */     return getServerByRegex(balancer, ip, "gray-ip");
/*    */   }
/*    */ 
/*    */   
/*    */   public String getName() {
/* 35 */     return this.name;
/*    */   }
/*    */ }


/* Location:              /Users/liupenghao/my-app/source/yiyi-service-gateway-0.0.3-20210909.123447-9.jar!/com/yiyi/service/gateway/gray/ServerIpStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */