package com.zcs.legion.proxy;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application.yml")
@Slf4j
public class HttpClientTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testPost() throws Exception{
        /*HashMap<String, String> map = new HashMap<>();
        map.put("tsm", "http://172.18.90.8:61614/tsm");
        String tagsJson = JSON.toJSONString(map);
        MockHttpServletResponse response = mvc.perform(
                post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tagsJson)).andReturn().getResponse();
        System.out.println(response.getContentAsString());
        mvc.perform(post("/m/agent/tsm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tagsJson))
                .andExpect(status().isOk());*/

    }
}
