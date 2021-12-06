import java.util.HashMap;
import java.util.Map;

/**
 * @author liuph
 * @desc
 * @date 2021/12/02 15:32
 */
public class OnlyTest {


    public static void main(String[] args) {

        String target = "(sum(rate(mq_message_consume_delay_time_histogram_bucket%7Bapplication%3D%22peppa-carnival-classroom-listener%22%2Ctopic%3D%22CARNIVAL_CLASSROOM_EVENT_TOPIC%22%2Cgroup%3D%22PEPPA-CARNIVAL_CLASSROOM_EVENT_CONSUMER%22%2Cle%3D%221000000.0%22%7D%5B1m%5D))-sum(rate(mq_message_consume_delay_time_histogram_bucket%7Bapplication%3D%22peppa-carnival-classroom-listener%22%2Ctopic%3D%22CARNIVAL_CLASSROOM_EVENT_TOPIC%22%2Cgroup%3D%22PEPPA-CARNIVAL_CLASSROOM_EVENT_CONSUMER%22%2Cle%3D%22500.0%22%7D%5B1m%5D)))%2Fsum(rate(mq_message_consume_delay_time_histogram_count%7Bapplication%3D%22peppa-carnival-classroom-listener%22%2Ctopic%3D%22CARNIVAL_CLASSROOM_EVENT_TOPIC%22%2Cgroup%3D%22PEPPA-CARNIVAL_CLASSROOM_EVENT_CONSUMER%22%7D%5B1m%5D))";

        System.out.println(replaceArray(target));
    }

    /**
     *  解密GET请求方式URL
     * @return  转译后的参数
     */
    public  static String replaceArray(String target){
        Map<String,String> map = new HashMap<String, String>(  );
        map.put ( "%24","$");
        map.put ( "%3A",":");
        map.put ( "%5B","[");
        map.put ( "%5D","]");
        map.put ( "%2C",",");
        map.put ( "%7B","{");
        map.put ( "%7D","}");
        map.put ( "%23","#");
        map.put ( "%22","\"");
        map.put ( "%5C","\\");
        map.put ( "%2D","-" );
        map.put ( "%20"," " );
        map.put ( "%21","!" );
        map.put ( "%25","%" );
        map.put ( "%26","&" );
        map.put ( "%27","'" );
        map.put ( "%28","(" );
        map.put ( "%29",")" );
        map.put ( "%2A","*" );
        map.put ( "%2B","+" );
        map.put ( "%2E","." );
        map.put ( "%2F","/" );
        map.put ( "%3B",";" );
        map.put ( "%3C","<" );
        map.put ( "%3D","=" );
        map.put ( "%3E",">" );
        map.put ( "%40","@" );
        map.put ( "%5E","^" );
        map.put ( "%5F","_" );
        map.put ( "%60","`" );
        map.put ( "%7C","|" );
        map.put ( "%7E","~" );
        map.put ( "%83","ƒ" );
        map.put ( "%85","…" );
        map.put ( "%86","†" );
        map.put ( "%87","‡" );
        map.put ( "%88","ˆ" );
        map.put ( "%89","‰" );
        map.put ( "%8A","Š" );
        map.put ( "%8B","‹" );
        map.put ( "%8C","Œ" );
        map.put ( "%8E","Ž" );
        map.put ( "%95","•" );
        map.put ( "%96","–" );
        map.put ( "%97","—" );
        map.put ( "%98","˜" );
        map.put ( "%99","™" );
        map.put ( "%9A","š" );
        map.put ( "%9B","›" );
        map.put ( "%9C","œ" );
        map.put ( "%9E","ž" );
        map.put ( "%9F","Ÿ" );
        map.put ( "%A2","¢" );
        map.put ( "%A3","£" );
        map.put ( "%A4","¤" );
        map.put ( "%A5","¥" );
        map.put ( "%A6","¦" );
        map.put ( "%A7","§" );
        map.put ( "%A8","¨" );
        map.put ( "%A9","©" );
        map.put ( "%AB","«" );
        map.put ( "%AC","¬" );
        map.put ( "%AE","®" );

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (target.contains(key)) {
                target = target.replace(key, value);
            }
        }

        return  target;
    }
}
