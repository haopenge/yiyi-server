package com.peppa.service.gateway.test;

import reactor.core.publisher.Mono;

public class ThisDemo {
    private String name = "ThisDemo";


    public void test() {
        (new Thread(new Runnable() {
            private String name = "Runnable";


            public void run() {
                System.out.println("这里的this指向匿名类:" + this.name);
            }
        })).start();


        (new Thread(() -> System.out.println("这里的this指向当前的ThisDemo类:" + this.name)))

                .start();
    }

    public static void main(String[] args) {
        Mono.just("String").subscribe(System.out::println);
        ThisDemo demo = new ThisDemo();
        demo.test();
    }
}
