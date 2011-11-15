/*
 * Copyright (c) 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chariotsolutions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.java.annotation.Listener;
import org.cometd.java.annotation.Service;
import org.cometd.java.annotation.Session;

@Named
@Singleton
@Service("helloService")
public class HelloService
{
    @Inject
    private BayeuxServer bayeux;
    @Session
    private ServerSession serverSession;

    private LocalSession localSession;

    @PostConstruct
    public void init()
    {
        localSession = bayeux.newLocalSession("local");
        localSession.handshake();

        for(;;) {
            
            try {
                Thread.sleep(250);
            } catch(Exception ex) {
                //
            }
            System.out.println("handshook? " + new Date());
            if(localSession.isHandshook()) {
                System.out.println("handshook!");
                new TimePublisher().start();
                break;
            }
        }
    }

    @Listener("/service/hello")
    public void processHello(ServerSession remote, ServerMessage.Mutable message)
    {
        Map<String, Object> input = message.getDataAsMap();
        String name = (String)input.get("name");

        Map<String, Object> output = new HashMap<String, Object>();
        output.put("greeting", "Hello, " + name);
        remote.deliver(serverSession, "/hello", output, null);
    }


    private class TimePublisher extends Thread {
         public void run() {
            
            ClientSessionChannel channel = localSession.getChannel("/hello");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

            while(true) {
                try {
                    Map<String, Object> output = new HashMap<String, Object>();
                    Date now = new Date();
                    output.put("time", dateFormat.format(now));
                    //output.put("value", Math.random());
                    output.put("value", Math.sin(now.getTime() % 360));
                    channel.publish(output);
                    try {
                        Thread.sleep(250);
                    } catch(Exception ex) {
                        System.out.println("publisher interrupted");
                        return;
                    }  
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }    
        }
    }

}



