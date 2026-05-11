package com.xrxs.plugin.open.demo.impl;

import com.xrxs.plugin.opensdk.pointcut.employee.field.handler.IdCodeVerifyHandler;
import com.xrxs.plugin.opensdk.pointcut.employee.field.model.FieldRuleVerificationRequestModel;
import com.xrxs.plugin.opensdk.pointcut.employee.field.model.GeneralValidationResultModel;
import org.springframework.stereotype.Component;

@Component
public class MyIdCodeVerifyHandler implements IdCodeVerifyHandler {

    public GeneralValidationResultModel afterHandle(FieldRuleVerificationRequestModel requestModel, String companyId, String employeeId, String idCode, GeneralValidationResultModel result) {

        if (idCode.startsWith("0")){
            result.error("身份证号非法, 不能以 0 开头!");
        }

        return result;
    }

}