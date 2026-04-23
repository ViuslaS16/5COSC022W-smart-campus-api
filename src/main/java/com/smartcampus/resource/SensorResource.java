package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Collection<Sensor> getAllSensors(@QueryParam("type") String type) {
        if (type == null || type.trim().isEmpty()) {
            return DataStore.sensors.values();
        }

        List<Sensor> filtered = new ArrayList<>();
        for (Sensor sensor : DataStore.sensors.values()) {
            if (type.equalsIgnoreCase(sensor.getType())) {
                filtered.add(sensor);
            }
        }
        return filtered;
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor.getRoomId() == null || !DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room ID provided does not exist.");
        }
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
             return Response.status(422).entity("Sensor ID cannot be null or empty").build();
        }

        Room room = DataStore.rooms.get(sensor.getRoomId());
        if (room.getSensorIds() != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        DataStore.sensors.put(sensor.getId(), sensor);

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String id) {
        // Sub-resource locator: handles all /sensors/{sensorId}/readings requests
        return new SensorReadingResource(id);
    }
}
