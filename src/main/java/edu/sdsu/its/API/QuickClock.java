package edu.sdsu.its.API;

import edu.sdsu.its.API.Models.SimpleMessage;
import edu.sdsu.its.API.Models.Staff;
import edu.sdsu.its.Welcome.Clock;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;

/**
 * @author Tom Paulus
 *         Created on 7/2/17.
 */
@Path("clock")
public class QuickClock {
    private static final Logger LOGGER = Logger.getLogger(QuickClock.class);

    /**
     * Get the status of a Staff Member's Clock
     *
     * @param id {@link int} Staff Member's ID
     * @return {@link Response} True = Clocked IN & False = Clocked OUT
     */
    @Path("status")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getClockStatus(@QueryParam("id") String id) {
        LOGGER.info("Received Request: [GET] CLOCK/STATUS - id = " + id);

        if (id == null || id.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new SimpleMessage("Error",
                            "No ID Specified").asJson())
                    .build();
        }

        id = new String(Base64.getDecoder().decode(id));

        Staff staff = Staff.getStaff(id);
        if (staff == null || !staff.clockable) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(
                    new SimpleMessage("Error",
                            "ID does not have a Clock."
                    ).asJson()).build();
        }

        final boolean status = new Clock(staff).getStatus();
        return Response.status(Response.Status.OK).entity(status).build();
    }

    /**
     * Toggle the status of a Staff Member's Clock
     *
     * @param id {@link int} Staff Member's ID
     * @return {@link Response} True = User was Clocked In & False = User was Clocked Out
     */
    @Path("toggle")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response toggleClock(@QueryParam("id") String id) {
        LOGGER.info("Received Request: [GET] CLOCK/TOGGLE - id = " + id);

        if (id == null || id.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new SimpleMessage("Error",
                            "No ID Specified").asJson())
                    .build();
        }

        id = new String(Base64.getDecoder().decode(id));

        Staff staff = Staff.getStaff(id);
        if (staff == null || !staff.clockable) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(
                    new SimpleMessage("Error",
                            "ID does not have a Clock."
                    ).asJson()).build();
        }

        final boolean status = new Clock(staff).toggle();
        return Response.status(Response.Status.ACCEPTED).entity(status).build();
    }
}
