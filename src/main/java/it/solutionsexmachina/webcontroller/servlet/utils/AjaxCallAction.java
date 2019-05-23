package it.solutionsexmachina.webcontroller.servlet.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.solutionsexmachina.webcontroller.annotation.ServiceMethod;
import it.solutionsexmachina.webcontroller.converter.BigDecimalCustomConverter;
import it.solutionsexmachina.webcontroller.exceptions.ValidationErrorsException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.log4j.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class AjaxCallAction {
    private static Logger log = Logger.getLogger(AjaxCallAction.class);

    private HttpServletRequest request;
    private HttpServletResponse response;

    public AjaxCallAction(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public static String parseCall(HttpServletRequest request, HttpServletResponse response) {
        String result = "{}";

        AjaxCallAction ajaxCallAction = new AjaxCallAction(request, response);
        AjaxCall ajaxCall = ajaxCallAction.getAjaxCallParameters(request);

        result = ajaxCallAction.callBeanMethod(ajaxCall);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Length", String.valueOf(result.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"response.json\"");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        return result;
    }

    public String callBeanMethod(AjaxCall ajaxCall) {
        DateTimeConverter dtConverter = new DateConverter();
        dtConverter.setPattern("dd/MM/yyyy");
        ConvertUtils.register(dtConverter, Date.class);
        BigDecimalCustomConverter bigDecimalCustomConverter = new BigDecimalCustomConverter(true);
        ConvertUtils.register(bigDecimalCustomConverter, BigDecimal.class);

        Object result = null;
        if (ajaxCall.getMethod() != null && ajaxCall.getParameters() != null) {
            try {
                Object actionBean = CDI.current().select(new NamedLiteral(ajaxCall.getBean())).get();
                MethodUtils.invokeMethod(actionBean, "setRequest", new Object[]{request, response});

                if (ajaxCall.getMethod() != null) {
                    Method[] methods = actionBean.getClass().getMethods();
                    for (Method method : methods) {
                        ServiceMethod annotation = method.getAnnotation(ServiceMethod.class);
                        if (annotation != null && annotation.value().equals(ajaxCall.getMethod())) {
                            Parameter[] parameters = method.getParameters();
                            Object parameterObject = null;
                            if (parameters.length > 0) {
                                Class<?> parameterType = parameters[0].getType();
                                parameterObject = parameterType.newInstance();

                                for (int i = 0; i < ajaxCall.getParameters().length; i++) {
                                    try {
                                        BeanUtils.setProperty(parameterObject, ajaxCall.getParameters()[i].getName(), ajaxCall.getParameters()[i].getValue());
                                    } catch (ConversionException e) {
                                        log.info("Could not set parameter " + ajaxCall.getParameters()[i].getName() + " on bean " + ajaxCall.getBean() + ", value= " + ajaxCall.getParameters()[i].getValue());

                                    }
                                }

                            }
                            result = MethodUtils.invokeMethod(actionBean, method.getName(), parameterObject);
                        }
                    }
                }
            }
            catch (Exception e) {
                if (e.getCause()!=null && e.getCause() instanceof ValidationErrorsException){
                    result = ((ValidationErrorsException)e.getCause()).getErrors();
                }
                e.printStackTrace();

            }
        }

        ObjectMapper mapper = new ObjectMapper();

        String response = "{}";
        try {
            response = mapper.writeValueAsString(result);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return response;

    }

    public AjaxCall getAjaxCallParameters(HttpServletRequest request2) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        String jsonCall = stringBuilder.toString();
        ;
        JSONObject jsonCallObject = JSONObject.fromObject(jsonCall);
        JSONArray jsonParametersObject = jsonCallObject.getJSONArray("parameters");

        jsonCallObject.discard("parameters");
        AjaxCall ajaxCall = (AjaxCall) JSONObject.toBean(jsonCallObject, AjaxCall.class);

        List<AjaxCallParameters> parameters = new ArrayList<AjaxCallParameters>();
        @SuppressWarnings("unchecked")
        Iterator<JSONObject> iterator = jsonParametersObject.iterator();
        while (iterator.hasNext()) {
            JSONObject jsonParameter = (JSONObject) iterator.next();
            parameters.add((AjaxCallParameters) JSONObject.toBean(jsonParameter, AjaxCallParameters.class));
        }

        ajaxCall.setParameters((AjaxCallParameters[]) parameters.toArray(new AjaxCallParameters[0]));

        return ajaxCall;

    }

}
