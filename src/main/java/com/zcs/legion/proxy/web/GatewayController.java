package com.zcs.legion.proxy.web;


import com.legion.client.common.LegionConnector;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * GatewayController
 * @author lance
 * @since 2019.2.23 15:06
 */
@Slf4j
@RestController
public class GatewayController {
    private final Counter REQUEST_TOTAL = Metrics.counter(" http_req_total", "Legion-Gateway", "reg_node_total");
    @Autowired
    private LegionConnector legionConnector;

    /**
     * 消息转发处理
     * @param type      消息类型, M/P
     * @param groupId   GroupID
     * @param tag       标签
     * @param body      消息体
     * @return          返回结果
     */
    @RequestMapping(value = "/{type}/{groupId}/{tag}", method = RequestMethod.POST)
    public String  dispatch(@PathVariable String type, @PathVariable String groupId,
                      @PathVariable String tag, @RequestBody String body) {
        REQUEST_TOTAL.increment();

        if(log.isDebugEnabled()){
            log.info("===>RequestURI: {}/{}/{}, body: {}", type, groupId, tag, body);
        }
        return "hello";
    }
}
