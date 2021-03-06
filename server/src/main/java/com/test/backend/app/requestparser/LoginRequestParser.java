package com.test.backend.app.requestparser;

import com.test.backend.app.model.User;
import com.test.backend.server.endpoint.RequestParser;
import com.test.backend.server.endpoint.RequestParserException;
import com.test.backend.server.http.Request;

import java.util.List;

/**
 * Created by ulises on 10/10/15.
 */
public class LoginRequestParser implements RequestParser<User> {

    public User parse(Request request) throws RequestParserException {

        List<String> pathParams = request.getPathParams().getVariables();
        if (pathParams.size() < 1) {
            throw new RequestParserException("Need to provide userId");
        } else {
            String userIdString = pathParams.get(0);
            return new User(Integer.valueOf(userIdString));
        }

    }
}
