/*    */ package com.yiyi.service.gateway.gray;
/*    */ 
/*    */ import com.netflix.loadbalancer.ILoadBalancer;
/*    */ import com.netflix.loadbalancer.Server;
/*    */ import com.yiyi.service.gateway.gray.factory.AnnoStrategy;
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ import java.util.StringTokenizer;
/*    */ import org.springframework.cloud.netflix.ribbon.eureka.EurekaServerIntrospector;
/*    */ import org.springframework.http.server.reactive.ServerHttpRequest;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @AnnoStrategy
/*    */ public class HeaderServerIplistStrategy
/*    */   extends StrategyAbstract
/*    */ {
/* 19 */   private final String header_key = "svips";
/*    */   
/*    */   public HeaderServerIplistStrategy() {
/* 22 */     setName("H_SVIP_");
/* 23 */     setOrder(2);
/*    */   }
/*    */ 
/*    */   
/*    */   public Server getServer(ILoadBalancer balancer, ServerHttpRequest request) {
/* 28 */     String svips = Strategy.getHeaderValue("svips", request);
/* 29 */     if (svips == null || svips.equals("")) {
/* 30 */       this.logger.info("HTTP header svips:---null!!!");
/* 31 */       return null;
/*    */     } 
/* 33 */     this.logger.info("HTTP header svips:---{}", svips);
/* 34 */     StringTokenizer stringTokenizer = new StringTokenizer(svips, ",");
/* 35 */     String serverDiscoverName = null;
/* 36 */     while (stringTokenizer.hasMoreElements()) {
/* 37 */       if (serverDiscoverName == null) {
/* 38 */         serverDiscoverName = getDiscoverName(balancer);
/* 39 */         if (serverDiscoverName == null) {
/* 40 */           return null;
/*    */         }
/*    */       } 
/* 43 */       String svip = stringTokenizer.nextToken();
/* 44 */       int ind = svip.indexOf(":");
/* 45 */       if (ind <= 0 || svip.length() == ind + 1) {
/*    */         continue;
/*    */       }
/* 48 */       String servername = svip.substring(0, ind).trim();
/* 49 */       String ip = svip.substring(ind + 1).trim();
/* 50 */       if (serverDiscoverName.equals(servername)) {
/* 51 */         Server retserver = getIpSameServer(ip, balancer);
/* 52 */         if (retserver != null) {
/* 53 */           return retserver;
/*    */         }
/*    */       } 
/*    */     } 
/* 57 */     return null;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   String getDiscoverName(ILoadBalancer balancer) {
/* 67 */     EurekaServerIntrospector serverIntrospector = new EurekaServerIntrospector();
/* 68 */     List<Server> servers = balancer.getReachableServers();
/* 69 */     Iterator<Server> iterator = servers.iterator(); if (iterator.hasNext()) { Server server = iterator.next();
/* 70 */       return server.getMetaInfo().getAppName(); }
/*    */     
/* 72 */     return null;
/*    */   }
/*    */ 
/*    */   
/*    */   public String getName() {
/* 77 */     return this.name;
/*    */   }
/*    */ }


/* Location:              /Users/liupenghao/my-app/source/yiyi-service-gateway-0.0.3-20210909.123447-9.jar!/com/yiyi/service/gateway/gray/HeaderServerIplistStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */