package org.nettymvc.exception;

/**
 * Created by myan on 12/7/2017.
 * Intellij IDEA
 */
enum ExceptionMessage {
    INITIALIZE_ERROR("Failed to initialize the framework"),
    ACTION_EXECUTE_ERROR("Can not execute such action."),
    INVALID_RESPONSE_ERROR("All response must be represented by \"org.nettymvc.data.response.Response\"."),
    INVALID_REQUEST_ERROR("We can not process request of this type at present.");
    
    private String message;
    
    ExceptionMessage(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}
