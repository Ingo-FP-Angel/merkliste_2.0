package de.theonebrack.merkliste_20.Models

import java.net.URLEncoder

data class LoginFormData(
        val FORM_SUBMIT: String,
        val REQUEST_TOKEN: String,
        val username: String,
        val password: String
) {
    override fun toString(): String {
        return "FORM_SUBMIT=" + URLEncoder.encode(FORM_SUBMIT, "utf-8") + "&REQUEST_TOKEN=" + URLEncoder.encode(REQUEST_TOKEN, "utf-8") + "&username=" + URLEncoder.encode(username, "utf-8") + "&password=" + URLEncoder.encode(password, "utf-8")
    }
}