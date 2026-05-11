package com.xrxs.plugin.opensdk.pointcut.employee.field.handler;

import com.xrxs.plugin.opensdk.pointcut.employee.field.model.FieldRuleVerificationRequestModel;
import com.xrxs.plugin.opensdk.pointcut.employee.field.model.GeneralValidationResultModel;

public class IdCodeStartValidator implements IdCodeVerifyHandler {

    @Override
    public Object[] beforeHandle(FieldRuleVerificationRequestModel requestModel, String companyId, String employeeId, String idCode) {
        if (idCode != null && idCode.startsWith("0")) {
            throw new RuntimeException("身份证号码不能以 0 开头");
        }
        return new Object[]{requestModel, companyId, employeeId, idCode};
    }

    @Override
    public GeneralValidationResultModel handle(FieldRuleVerificationRequestModel requestModel, String companyId, String employeeId, String idCode) {
        return null;
    }

    @Override
    public GeneralValidationResultModel afterHandle(FieldRuleVerificationRequestModel requestModel, String companyId, String employeeId, String idCode, GeneralValidationResultModel result) {
        return result;
    }
}
