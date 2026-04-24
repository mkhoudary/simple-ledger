/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package mk.projects.simpleledger.rest.errors;

import mk.projects.simpleledger.utils.GsonUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Mohammad
 */
public final class ApiErrorResponse {

    private ApiErrorResponse() {
    }

    public static String build(int status, String message, String path) {
        GsonUtils.JsonObjectBuilder builder = GsonUtils.jsonObjectBuilder()
                .prop("status", status)
                .prop("error", StringUtils.defaultIfBlank(message, "Unexpected error"));

        if (StringUtils.isNotBlank(path)) {
            builder.prop("path", path);
        }

        return builder.build().toString();
    }
}
