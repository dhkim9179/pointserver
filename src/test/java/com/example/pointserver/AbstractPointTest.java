package com.example.pointserver;

import com.example.pointserver.cancel.CancelService;
import com.example.pointserver.common.utils.JsonUtils;
import com.example.pointserver.earn.EarnService;
import com.example.pointserver.earn.model.EarnCancelRequest;
import com.example.pointserver.earn.model.EarnRequest;
import com.example.pointserver.expire.ExpireService;
import com.example.pointserver.history.HistoryService;
import com.example.pointserver.use.UseService;
import com.example.pointserver.use.detail.UseDetailService;
import com.example.pointserver.use.model.UseCancelRequest;
import com.example.pointserver.use.model.UseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@ActiveProfiles("local")
@SpringBootTest
public class AbstractPointTest {
    @Autowired
    protected Environment environment;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EarnService earnService;

    @Autowired
    protected UseService useService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected ExpireService expireService;

    @Autowired
    protected UseDetailService useDetailService;

    @Autowired
    protected CancelService cancelService;

    public MockHttpServletResponse requestEarn(EarnRequest earnRequest) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/point/v1/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(earnRequest))
        ).andReturn().getResponse();
    }

    public MockHttpServletResponse requestEarnCancel(EarnCancelRequest earnCancelRequest) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/point/v1/earn/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(earnCancelRequest))
        ).andReturn().getResponse();
    }

    public MockHttpServletResponse requestUse(UseRequest useRequest) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/point/v1/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(useRequest))
        ).andReturn().getResponse();
    }

    public MockHttpServletResponse requestUseCancelAll(UseCancelRequest useCancelRequest) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/point/v1/use/cancel/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(useCancelRequest))
        ).andReturn().getResponse();
    }

    public MockHttpServletResponse requestUseCancelPartial(UseCancelRequest useCancelRequest) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/point/v1/use/cancel/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(useCancelRequest))
        ).andReturn().getResponse();
    }
}
