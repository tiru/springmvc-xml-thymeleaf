package com.hellokoding.springmvc;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellokoding.springmvc.services.WebPushMessage;
import com.hellokoding.springmvc.services.WebPushSubscription;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

@Controller
@RequestMapping("/")
public class HelloController {
	
	private static PushService pushService = new PushService();
	
	private Map<String, WebPushSubscription> subscriptions = new ConcurrentHashMap<>();

	//@Autowired
	
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello(ModelMap model) {
       // model.addAttribute("name", name);
        // Add BouncyCastle as an algorithm provider
		  if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
		      Security.addProvider(new BouncyCastleProvider());
		  }
		
		try {
			pushService.setPublicKey("BENGlPcmp7oJo4QaLJXFgk5-e3W5_Rywr3KDF6KXjtbxfOlgk73az9w2Jfj7iZ8JgH9KgdH75s8hggrY0-7k-F8=");
			pushService.setPrivateKey("dQhGsMq6FuHuw1Tnz_8YxM0O78-GiqOHFdrW48rGCgk=");
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return "hello";
    }
    
	@RequestMapping(value = "/subscribe" ,method = RequestMethod.POST ,produces = "application/json")
	public void subscribe(@RequestBody WebPushSubscription subscription) {
		
		subscriptions.put(subscription.getNotificationEndPoint(), subscription);
	}
	
	@RequestMapping(value = "/unsubscribe",method = RequestMethod.POST ,produces = "application/json")
	public void unsubscribe(@RequestBody WebPushSubscription subscription) {
		
		subscriptions.remove(subscription.getNotificationEndPoint());
	}
    
	@RequestMapping(value = "/notify-all",method = RequestMethod.POST,produces = "application/json" )
	public WebPushMessage notifyAll(@RequestBody WebPushMessage message) throws GeneralSecurityException, IOException, JoseException, ExecutionException, InterruptedException {
    	 ObjectMapper objectMapper = new ObjectMapper();
		for (WebPushSubscription subscription: subscriptions.values()) {
			
			Notification notification = new Notification(
					subscription.getNotificationEndPoint(),
					subscription.getPublicKey(),
					subscription.getAuth(),
					objectMapper.writeValueAsBytes(message));
			
			pushService.send(notification);			
		}
		
		return message;		
	}
}
