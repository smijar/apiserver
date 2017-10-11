package com.app.apiserver.core;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Aggregate for openstack hosts that groups together a list of hosts
 * that comprise a satellite location.
 * 
 * - has metadata that contains the "zone" and "latitude"/"longitude" for the satellite
 * 
 * @author smijar
 *
 */
public class ClientSessionData {

	private String version;
	
	@JsonIgnore
	private Date start_time;
	
	@JsonIgnore
	private Date stop_time;
	
	private double duration_in_seconds;
	private boolean client_used_cell_carrier;
	private double total_tx_bytes_to_client;
	private double total_rx_bytes_from_client;
	private double total_tx_and_rx_bytes;	
	
	private List<ClientSessionChannelsData> channels;
	
	public ClientSessionData() { } // default constructor needed by json conversion code

	@JsonProperty
	public String getStart_time() {
		if( start_time != null ) {
			return ISODateTimeFormat.dateTime().print(new DateTime(this.start_time));
		}
		return "";
	}

	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}

	@JsonProperty
	public String getStop_time() {
		if( stop_time != null ) {
			return ISODateTimeFormat.dateTime().print(new DateTime(this.stop_time));
		}
		return "";
	}

	public void setStop_time(Date stop_time) {
		this.stop_time = stop_time;
	}

	public double getDuration_in_seconds() {
		return duration_in_seconds;
	}

	public void setDuration_in_seconds(double duration_in_seconds) {
		this.duration_in_seconds = duration_in_seconds;
	}

	public boolean isClient_used_cell_carrier() {
		return client_used_cell_carrier;
	}

	public void setClient_used_cell_carrier(boolean client_used_cell_carrier) {
		this.client_used_cell_carrier = client_used_cell_carrier;
	}

	public double getTotal_tx_bytes_to_client() {
		return total_tx_bytes_to_client;
	}

	public void setTotal_tx_bytes_to_client(double total_tx_bytes_to_client) {
		this.total_tx_bytes_to_client = total_tx_bytes_to_client;
	}

	public double getTotal_rx_bytes_from_client() {
		return total_rx_bytes_from_client;
	}

	public void setTotal_rx_bytes_from_client(double total_rx_bytes_from_client) {
		this.total_rx_bytes_from_client = total_rx_bytes_from_client;
	}

	public double getTotal_tx_and_rx_bytes() {
		return total_tx_and_rx_bytes;
	}

	public void setTotal_tx_and_rx_bytes(double total_tx_and_rx_bytes) {
		this.total_tx_and_rx_bytes = total_tx_and_rx_bytes;
	}

	public List<ClientSessionChannelsData> getChannels() {
		return channels;
	}

	public void setChannels(List<ClientSessionChannelsData> channels) {
		this.channels = channels;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	
}
