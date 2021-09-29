/*    */ package com.peppa.service.gateway.gray;
/*    */ 
/*    */ import com.netflix.loadbalancer.ILoadBalancer;
/*    */ import com.netflix.loadbalancer.Server;
/*    */ import com.peppa.service.gateway.gray.factory.AnnoStrategy;
/*    */ import java.util.StringTokenizer;
/*    */ import org.springframework.http.server.reactive.ServerHttpRequest;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @AnnoStrategy
/*    */ public class HeaderIplistStrategy
/*    */   extends StrategyAbstract
/*    */ {
/* 17 */   private final String header_key = "ips";
/*    */   
/*    */   public HeaderIplistStrategy() {
/* 20 */     setName("H_IPS_");
/* 21 */     setOrder(1);
/*    */   }
/*    */ 
/*    */   
/*    */   public Server getServer(ILoadBalancer balancer, ServerHttpRequest request) {
/* 26 */     String ips = Strategy.getHeaderValue("ips", request);
/* 27 */     if (ips == null || ips.equals("")) {
/* 28 */       this.logger.info("HTTP header ips:---null!!!");
/* 29 */       return null;
/*    */     } 
/* 31 */     this.logger.info("HTTP header ips:---{}", ips);
/* 32 */     StringTokenizer stringTokenizer = new StringTokenizer(ips, ",");
/* 33 */     while (stringTokenizer.hasMoreTokens()) {
/* 34 */       String ip = stringTokenizer.nextToken().trim();
/* 35 */       Server retserver = getIpSameServer(ip, balancer);
/* 36 */       if (retserver != null) {
/* 37 */         return retserver;
/*    */       }
/*    */     } 
/* 40 */     return null;
/*    */   }
/*    */ 
/*    */   
/*    */   public String getName() {
/* 45 */     return this.name;
/*    */   }
/*    */ }


/* Location:              /Users/liupenghao/my-app/source/peppa-service-gateway-0.0.3-20210909.123447-9.jar!/com/peppa/service/gateway/gray/HeaderIplistStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */