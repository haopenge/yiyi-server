/*     */ package com.yiyi.service.gateway.gray;
/*     */ 
/*     */ import com.netflix.loadbalancer.ILoadBalancer;
/*     */ import com.netflix.loadbalancer.Server;
/*     */ import com.yiyi.service.gateway.gray.factory.AnnoStrategy;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.StringTokenizer;
/*     */ import org.springframework.cloud.netflix.ribbon.eureka.EurekaServerIntrospector;
/*     */ import org.springframework.http.server.reactive.ServerHttpRequest;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @AnnoStrategy
/*     */ public class ServerTagStrategy
/*     */   extends StrategyAbstract
/*     */ {
/*  21 */   private final String strategy_key = "gray-tag";
/*     */   
/*     */   public ServerTagStrategy() {
/*  24 */     setName("S_TAG_");
/*  25 */     setOrder(3);
/*     */   }
/*     */ 
/*     */   
/*     */   public Server getServer(ILoadBalancer balancer, ServerHttpRequest request) {
/*     */     try {
/*  31 */       EurekaServerIntrospector serverIntrospector = new EurekaServerIntrospector();
/*  32 */       Server serverRet = null;
/*  33 */       int count = 0;
/*     */ 
/*     */       
/*  36 */       while (serverRet == null && count++ < 5) {
/*  37 */         List<Server> servers = balancer.getAllServers();
/*  38 */         List<Server> serversReach = balancer.getReachableServers();
/*  39 */         if (serversReach.size() == 0 || servers.size() == 0) {
/*  40 */           return null;
/*     */         }
/*     */ 
/*     */         
/*  44 */         List<Server> devlist = new ArrayList<>();
/*  45 */         for (Server server : servers) {
/*  46 */           Map metadata = serverIntrospector.getMetadata(server);
/*  47 */           if (metadata != null && metadata.containsKey("gray-tag")) {
/*  48 */             String metavalue = (String)metadata.get("gray-tag");
/*  49 */             if (metavalue.equals("no")) {
/*     */               continue;
/*     */             }
/*     */             try {
/*  53 */               if (metavalue != null && !metavalue.equals("")) {
/*  54 */                 StringTokenizer stringTokenizer = new StringTokenizer(metavalue, ",", false);
/*  55 */                 while (stringTokenizer.hasMoreTokens()) {
/*  56 */                   String stategy = stringTokenizer.nextToken().trim();
/*  57 */                   int ind = stategy.indexOf(":");
/*  58 */                   if (ind <= 0 || stategy.length() == ind + 1) {
/*     */                     continue;
/*     */                   }
/*  61 */                   String skey = stategy.substring(0, ind).trim();
/*  62 */                   String svalue = stategy.substring(ind + 1).trim();
/*  63 */                   String clientheadervalue = Strategy.getHeaderValue(skey, request);
/*  64 */                   if (clientheadervalue != null && clientheadervalue.equals(svalue)) {
/*  65 */                     devlist.add(server);
/*     */                   }
/*     */                 }
/*     */               
/*     */               }
/*     */             
/*  71 */             } catch (Exception e) {
/*  72 */               this.logger.error("gray-tag", e.getMessage());
/*     */             } 
/*     */           } 
/*     */         } 
/*  76 */         if (devlist.size() == 0) {
/*  77 */           return null;
/*     */         }
/*  79 */         serverRet = getRandom(devlist);
/*     */         
/*  81 */         if (serverRet == null) {
/*  82 */           Thread.yield();
/*     */         }
/*  84 */         else if (serverRet.isAlive() && serverRet.isReadyToServe()) {
/*  85 */           return serverRet;
/*     */         } 
/*     */         
/*  88 */         serverRet = null;
/*     */       } 
/*     */       
/*  91 */       if (count >= 5) {
/*  92 */         this.logger.warn("No available alive servers after 5 tries from load balancer: " + balancer);
/*     */       }
/*     */       
/*  95 */       return serverRet;
/*     */     }
/*  97 */     catch (Exception e) {
/*  98 */       this.logger.error(this.name.concat("选择异常"), e.getMessage());
/*     */       
/* 100 */       return null;
/*     */     } 
/*     */   }
/*     */   
/*     */   public String getName() {
/* 105 */     return this.name;
/*     */   }
/*     */ }


/* Location:              /Users/liupenghao/my-app/source/yiyi-service-gateway-0.0.3-20210909.123447-9.jar!/com/yiyi/service/gateway/gray/ServerTagStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */