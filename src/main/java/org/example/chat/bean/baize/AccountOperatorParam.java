package org.example.chat.bean.baize;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
public class AccountOperatorParam implements Serializable {

    private int accountId;
    private String callBackUrl;
    private String smsCallbackUrl;
    private String smsMoCallbackUrl;
    private String taskCallbackUrl;
    private String callSmsCallbackUrl;
    private String callDataCallbackUrl;
    private String callUpdateCallbackUrl;
    private String callMCallbackUrl;
//    private String callbackStatusConfig;
//    private String callbackFieldConfig;
    private List<OutboundType> callBackRange;
    private List<OutboundType> dataStatisticRange;
    private List<String> whiteIps;
    private String aes;
    private String salt;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
