import com.husky.intf.EatService;
import feign.Feign;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import java.lang.reflect.Proxy;

public class FeignTest {

    @Test
    public void eatApple() {
        EatService eatService = Feign.builder()
                .contract(new SpringMvcContract())
                .target(EatService.class, "http://localhost:18081/eat");

        String s = eatService.eatApple();

        System.out.println(" eatService.eatApple() = " + s);
    }

    @Test
    public void test() {
        Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{EatService.class}, (proxy, method, args) -> {
            return proxy;
        });
    }
}
