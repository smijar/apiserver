package com.example.kafka.example1;

import java.util.Properties;
import java.util.Scanner;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

public class Producer {
    private static Scanner in;
    public static void main(String[] argv)throws Exception {
        if (argv.length != 1) {
            System.err.println("Please specify the topic name.");
            System.exit(-1);
        }
        
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");

        org.apache.kafka.clients.producer.Producer producer = new KafkaProducer<String, String>(configProperties);
        
        try {
	        String topicName = argv[0];
	        in = new Scanner(System.in);
	        System.out.println("Producer: Enter message(type exit to quit) for topic:"+topicName);
	
	        //Configure the Producer

	        String line = in.nextLine();
	        while(!line.equals("exit")) {
	            ProducerRecord<String, String> rec = new ProducerRecord<String, String>(topicName, line);
	            producer.send(rec);
	            line = in.nextLine();
	        }
        }
        finally {
	        in.close();
	        producer.close();
        }
    }
  }
