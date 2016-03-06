package com.numberoverzero.snippets.dwhello.resources;

import com.numberoverzero.snippets.dwhello.annotations.OtherParam;
import com.numberoverzero.snippets.dwhello.annotations.ProviderParam;
import com.numberoverzero.snippets.dwhello.annotations.TokenParam;
import com.numberoverzero.snippets.dwhello.core.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Path("/")
public class PersonResource {
    private final Function<String, Person> getPerson;
    private final BiConsumer<String, Person> putPerson;

    public PersonResource(
            Function<String, Person> getPerson,
            BiConsumer<String, Person> putPerson) {
        this.getPerson = getPerson;
        this.putPerson = putPerson;
    }

    @GET
    @Path("person/{name}")
    @Produces("application/json")
    public Response get(
            @PathParam("name") String name,
            @TokenParam Token token,
            @Context HttpHeaders headers,
            @OtherParam Token token2,
            @ProviderParam Token token3) {
        System.out.println(headers.getRequestHeaders());
        System.out.println("Token: " + token.getToken());
        System.out.println("Token2: " + token2.getToken());
        System.out.println("Token3: " + token3.getToken());

        Person person = this.getPerson.apply(name);
        if (person == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(person).build();
    }

    @POST
    @Path("person")
    @Consumes("application/json")
    public void post(Person person) {
        this.putPerson.accept(person.name, person);
    }
}
