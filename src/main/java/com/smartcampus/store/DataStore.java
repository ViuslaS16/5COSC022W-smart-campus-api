package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe singleton in-memory data store for the application data.
 * Used ConcurrentHashMap to fulfill requirements safely across concurrent requests.
 */
public class DataStore {
    public static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();
    
    // Private constructor to prevent instantiation
    private DataStore() {}
}
