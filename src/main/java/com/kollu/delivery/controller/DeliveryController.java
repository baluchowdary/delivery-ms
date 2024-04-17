package com.kollu.delivery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kollu.delivery.dto.CustomerOrder;
import com.kollu.delivery.dto.DeliveryEvent;
import com.kollu.delivery.entity.Delivery;
import com.kollu.delivery.entity.DeliveryRepository;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DeliveryController {

	@Autowired
	private DeliveryRepository repository;

	@Autowired
	private KafkaTemplate<String, DeliveryEvent> kafkaTemplate;
	
	private static final String NEW_STOCK_TOPIC = "new-stock";
	private static final String NEW_STOCK_GROUP = "stock-group";

	@KafkaListener(topics = NEW_STOCK_TOPIC, groupId = NEW_STOCK_GROUP)
	public void deliverOrder(String event) throws JsonMappingException, JsonProcessingException {
		System.out.println("Inside ship order for order "+event);
		log.info("deliverOrder  -- Method start");
		
		Delivery shipment = new Delivery();
		DeliveryEvent inventoryEvent = new ObjectMapper().readValue(event, DeliveryEvent.class);
		CustomerOrder order = inventoryEvent.getOrder();

		try {
			if (order.getAddress() == null) {
				throw new Exception("Address not present");
			}

			shipment.setAddress(order.getAddress());
			shipment.setOrderId(order.getOrderId());

			shipment.setStatus("success");

			repository.save(shipment);
			
			log.info("deliverOrder  -- Method end");
		} catch (Exception e) {
			shipment.setOrderId(order.getOrderId());
			shipment.setStatus("failed");
			repository.save(shipment);

			System.out.println(order);

			DeliveryEvent reverseEvent = new DeliveryEvent();
			reverseEvent.setType("STOCK_REVERSED");
			reverseEvent.setOrder(order);
			kafkaTemplate.send("reversed-stock", reverseEvent);
		}
	}
}
