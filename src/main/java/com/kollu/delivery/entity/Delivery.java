package com.kollu.delivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "Delivery_Table")
public class Delivery {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "Delivery_Address")
	private String address;

	@Column(name = "Delivery_Status")
	private String status;

	@Column(name = "Delivery_OrderId")
	private long orderId;


}
