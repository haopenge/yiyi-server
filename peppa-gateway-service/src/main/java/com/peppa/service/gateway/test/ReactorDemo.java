package com.peppa.service.gateway.test;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;


public class ReactorDemo {
    public static void main(String[] args) {
        String[] strs = {"1", "2", "3"};

        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            private Subscription subscription;


            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;


                this.subscription.request(1L);
            }


            public void onNext(Integer item) {
                System.out.println("接受到数据: " + item);

                try {
                    TimeUnit.SECONDS.sleep(3L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                this.subscription.request(1L);
            }


            public void onError(Throwable throwable) {
                throwable.printStackTrace();


                this.subscription.cancel();
            }


            public void onComplete() {
                System.out.println("处理完了!");
            }
        };


        Flux.fromArray((Object[]) strs).map(s -> Integer.parseInt((String) s))
                .subscribe(subscriber);
    }
}


