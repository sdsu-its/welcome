package edu.sdsu.its.fit_welcome;

import edu.sdsu.its.fit_welcome.Models.Quote;
import edu.sdsu.its.fit_welcome.Models.Staff;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;

/**
 * Admin Interfaces/Pages
 *
 * @author Tom Paulus
 *         Created on 1/1/16.
 */
@Path("admin")
public class Admin {
    private static Logger Log = Logger.getLogger(Admin.class);

    /**
     * Admin Page
     *
     * @param id          {@link String} User's ID
     * @param adminAction {@link String} Requested Action. If null options page is displayed.
     * @return {@link Response} Response
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_HTML)
    public Response admin(@QueryParam("id") final String id, @QueryParam("action") final String adminAction) {
        Log.info(String.format("Recieved Request: [GET] ADMIN - id = %s & action - %s", id, adminAction));

        Staff staff = id != null ? Staff.getStaff(Integer.parseInt(id)) : null;
        if (staff == null || !staff.admin) {
            return Response.status(Response.Status.FORBIDDEN).entity(Pages.makePage(Pages.FORBIDDEN, new HashMap<String, String>())).build();
        }

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("REDID", id);

        if (adminAction == null) {
            return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.ADMIN, params)).build();
        }

        if ("manual time".equals(adminAction)) {
            params.put("STAFFUSERS", Pages.arrayToList(Staff.getAllStaff("WHERE clockable = 1")));
            return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.MANUAL_TIME, params)).build();
        }

        if ("email timesheets".equals(adminAction)) {
            params.put("REPORT", "Staff Timesheets");
            params.put("RTYPE", "timesheets");
            return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.REPORT_DATE_PICKER, params)).build();
        }

        if ("new staff".equals(adminAction)) {
            return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.NEW_USER, params)).build();
        }

        if ("run report".equals(adminAction)) {
            params.put("REPORT", "Usage Report");
            params.put("RTYPE", "usage");
            return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.REPORT_DATE_PICKER, params)).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Request").build();

    }

    /**
     * Reports Pages
     *
     * @param id         {@link String} User's ID
     * @param reportType {@link String} Which Type of report to Run.
     *                   Either 'timesheets' or 'usage'
     * @param startDate  {@link String} Start Date for the Report
     * @param endDate    {@link String} End Date for the Report
     * @return {@link Response} Response
     */
    @Path("report")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_HTML)
    public Response report(@QueryParam("id") final String id, @QueryParam("report_type") final String reportType, @QueryParam("start") final String startDate, @QueryParam("end") final String endDate) {
        Log.info(String.format("Recieved Request: [GET] ADMIN/REPORT - id = %s & report_type - %s & start - %s & end - %s", id, reportType, startDate, endDate));

        final Staff staff = id != null ? Staff.getStaff(Integer.parseInt(id)) : null;
        if (staff == null || !staff.admin) {
            return Response.status(Response.Status.FORBIDDEN).entity(Pages.makePage(Pages.FORBIDDEN, new HashMap<String, String>())).build();
        }

        final Quote quote = Quote.getRandom();

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("FIRST", staff.firstName);
        params.put("QUOTE", quote.text);
        params.put("QUOTEAUTHOR", quote.author);

        if ("timesheets".equals(reportType)) {
            new Thread() {
                @Override
                public void run() {
                    Log.info("Starting new Thread to Generate Timesheets");

                    final Staff[] allClockableStaff = DB.getAllStaff("WHERE clockable = 1");
                    File[] timesheets = new File[allClockableStaff.length];
                    for (int s = 0; s < allClockableStaff.length; s++) {
                        final int staffId = allClockableStaff[s].id;
                        final String staffLast = allClockableStaff[s].lastName;
                        timesheets[s] = Timesheet.make(DB.exportClockIOs(staffId, startDate, endDate), staffLast);
                    }

                    new SendEmail().emailFile("Staff Timesheets", staff.firstName, timesheets).send(staff.email);
                }
            }.start();

            return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.REPORT_CONF, params)).build();
        }

        if ("usage".equals(reportType)) {
            new Thread() {
                @Override
                public void run() {
                    Log.info("Starting new Thread to Generate Usage Report");

                    new SendEmail().emailFile("Events Report", staff.firstName, new File[]{DB.exportEvents(startDate, endDate, "events")}).send(staff.email);
                }
            }.start();

            return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.REPORT_CONF, params)).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Request").build();
    }

    /**
     * Manual Time Entry Page
     *
     * @param id     {@link String} User's ID
     * @param userID {@link String} ID of user who will be clocked in/out
     * @param action {@link String} Action to be performed.
     *               Either: 'clockIn' or 'clockOut'
     * @param date   {@link String} Date and Time the action should be performed
     *               In HTML datetime-locale format
     * @return {@link Response} Response
     */
    @Path("manual_time")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_HTML)
    public Response manualTime(@QueryParam("id") final String id, @QueryParam("user") final String userID, @QueryParam("action") final String action, @QueryParam("date") final String date) {
        Log.info(String.format("Recieved Request: [GET] ADMIN/MANUAL_TIME - id = %s & userID - %s & action - %s & date - %s", id, userID, action, date));

        Staff staff = id != null ? Staff.getStaff(Integer.parseInt(id)) : null;
        if (staff == null || !staff.admin) {
            return Response.status(Response.Status.FORBIDDEN).entity(Pages.makePage(Pages.FORBIDDEN, new HashMap<String, String>())).build();
        }

        final Quote quote = Quote.getRandom();

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("FIRST", staff.firstName);
        params.put("QUOTE", quote.text);
        params.put("QUOTEAUTHOR", quote.author);

        final Staff staff1 = Staff.getStaff(userID);

        if ("clockIn".equals(action)) {
            DB.clockIn(Integer.parseInt(userID), "STR_TO_DATE('" + date + "','%Y-%m-%dT%H:%i')");
            params.put("ACTION", String.format("Manually Clocked In %s %s", staff1.firstName, staff1.lastName));
        } else if ("clockOut".equals(action)) {
            DB.clockOut(Integer.parseInt(userID), "STR_TO_DATE('" + date + "','%Y-%m-%dT%H:%i')");
            params.put("ACTION", String.format("Manually Clocked Out %s %s", staff1.firstName, staff1.lastName));
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Request").build();
        }

        return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.ADMIN_CONF, params)).build();
    }

    /**
     * New Staff User Creation Page
     *
     * @param id                     {@link String} User's ID
     * @param userID                 {@link String} New User's ID
     * @param userFirst              {@link String} New User's First Name
     * @param userLast               {@link String} New User's Last Name
     * @param email                  {@link String} New User's Email Address
     * @param clockable              {@link String}
     *                               Either: '0' or '1'
     * @param admin                  {@link String}
     *                               Either: '0' or '1'
     * @param instructional_designer {@link String}
     *                               Either: '0' or '1'
     * @return {@link Response} Response
     */
    @Path("create_user")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_HTML)
    public Response createUser(@QueryParam("id") final String id, @QueryParam("user_id") final String userID, @QueryParam("user_first") final String userFirst,
                               @QueryParam("user_last") final String userLast, @QueryParam("email") final String email, @QueryParam("clockable") final String clockable, @QueryParam("admin") final String admin,
                               @QueryParam("instructional_designer") final String instructional_designer) {
        Log.info(String.format("Recieved Request: [GET] ADMIN/CREATE_USER - id = %s & user_id - %s & user_first - %s & user_last - %s & clockable - %s & admin - %s", id, userID, userFirst, userLast, clockable, admin));

        final Staff staff = id != null ? Staff.getStaff(Integer.parseInt(id)) : null;
        if (staff == null || !staff.admin) {
            return Response.status(Response.Status.FORBIDDEN).entity(Pages.makePage(Pages.FORBIDDEN, new HashMap<String, String>())).build();
        }

        final Quote quote = Quote.getRandom();

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("FIRST", staff.firstName);
        params.put("QUOTE", quote.text);
        params.put("QUOTEAUTHOR", quote.author);

        final Staff staff1 = new Staff(Integer.parseInt(userID), userFirst, userLast, email, clockable.equals("1"), admin.equals("1"), instructional_designer.equals("1"));
        if (!DB.createNewStaff(staff1)) {
            params.put("ACTION", "Entered an ID that already exists");
            return Response.status(Response.Status.BAD_REQUEST).entity(Pages.makePage(Pages.ADMIN_CONF, params)).build();
        } else {
            params.put("ACTION", "successfully created a new Staff user");
            return Response.status(Response.Status.OK).entity(Pages.makePage(Pages.ADMIN_CONF, params)).build();
        }
    }
}