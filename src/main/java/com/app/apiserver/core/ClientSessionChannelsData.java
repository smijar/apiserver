package com.app.apiserver.core;



/**
 * Aggregate for openstack hosts that groups together a list of hosts
 * that comprise a satellite location.
 * 
 * - has metadata that contains the "zone" and "latitude"/"longitude" for the satellite
 * 
 * @author smijar
 *
 */
public class ClientSessionChannelsData {

	private String name;
    private double tx_message_count;
    private double tx_total_bytes;
    private double tx_min_message_size;
    private double tx_max_message_size;
    private double tx_avg_message_size;
    private double tx_stdev_message_size;
    private double rx_message_count;
    private double rx_total_bytes;
    private double rx_min_message_size;
    private double rx_max_message_size;
    private double rx_avg_message_size;
    private double rx_stdev_message_size;
	
	public ClientSessionChannelsData() { } // default constructor needed by json conversion code

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getTx_message_count() {
		return tx_message_count;
	}

	public void setTx_message_count(double tx_message_count) {
		this.tx_message_count = tx_message_count;
	}

	public double getTx_total_bytes() {
		return tx_total_bytes;
	}

	public void setTx_total_bytes(double tx_total_bytes) {
		this.tx_total_bytes = tx_total_bytes;
	}

	public double getTx_min_message_size() {
		return tx_min_message_size;
	}

	public void setTx_min_message_size(double tx_min_message_size) {
		this.tx_min_message_size = tx_min_message_size;
	}

	public double getTx_max_message_size() {
		return tx_max_message_size;
	}

	public void setTx_max_message_size(double tx_max_message_size) {
		this.tx_max_message_size = tx_max_message_size;
	}

	public double getTx_avg_message_size() {
		return tx_avg_message_size;
	}

	public void setTx_avg_message_size(double tx_avg_message_size) {
		this.tx_avg_message_size = tx_avg_message_size;
	}

	public double getTx_stdev_message_size() {
		return tx_stdev_message_size;
	}

	public void setTx_stdev_message_size(double tx_stdev_message_size) {
		this.tx_stdev_message_size = tx_stdev_message_size;
	}

	public double getRx_message_count() {
		return rx_message_count;
	}

	public void setRx_message_count(double rx_message_count) {
		this.rx_message_count = rx_message_count;
	}

	public double getRx_total_bytes() {
		return rx_total_bytes;
	}

	public void setRx_total_bytes(double rx_total_bytes) {
		this.rx_total_bytes = rx_total_bytes;
	}

	public double getRx_min_message_size() {
		return rx_min_message_size;
	}

	public void setRx_min_message_size(double rx_min_message_size) {
		this.rx_min_message_size = rx_min_message_size;
	}

	public double getRx_max_message_size() {
		return rx_max_message_size;
	}

	public void setRx_max_message_size(double rx_max_message_size) {
		this.rx_max_message_size = rx_max_message_size;
	}

	public double getRx_avg_message_size() {
		return rx_avg_message_size;
	}

	public void setRx_avg_message_size(double rx_avg_message_size) {
		this.rx_avg_message_size = rx_avg_message_size;
	}

	public double getRx_stdev_message_size() {
		return rx_stdev_message_size;
	}

	public void setRx_stdev_message_size(double rx_stdev_message_size) {
		this.rx_stdev_message_size = rx_stdev_message_size;
	}



	
}
