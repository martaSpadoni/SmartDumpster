/*Authors: Matteo Ragazzini, Marta Spadoni*/
package progetto03.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class DumpsterService extends AbstractVerticle {
	private final static int OLDER = +7;
	private final static Double MAX_WEIGHT = 500.0;
	private int port;
	private State state = State.AVAILABLE;
	private Double totalWeight = 0.0;
	private Double availability = MAX_WEIGHT;
	private int nDeposit = 0;
	private Optional<Deposit> lastDeposit;
	private boolean depositFlag = false;
	private boolean tokenTaken = false;
	
	 public DumpsterService(int port) {	
		this.port = port;
	}

	@Override
	public void start() {		
		Router router = Router.router(vertx);
		
		router.route("/sd-dashboard/*").handler(StaticHandler.create("sd-dashboard"));
		router.route("/api/esp*").handler(BodyHandler.create());
		router.route("/api/dash*").handler(BodyHandler.create());
		router.route("/api/app*").handler(BodyHandler.create());
		
		router.post("/api/esp").handler(this::updateWeight);
		router.get("/api/esp").handler(this::getState);
		
		router.get("/api/app").handler(this::tokenRequest);
		router.post("/api/app").handler(this::setNewDeposit);
		
		router.post("/api/dash").handler(this::handleState);
		router.get("/api/dash").handler(this::getStatus);
		router.get("/api/dash/statistics").handler(this::getStatistics);
		
		
		vertx
			.createHttpServer()
			.requestHandler(router::accept)
			.listen(port);

		log("Service ready.");
	}
	
	
	private List<Deposit> readDepositLog(){
		List<Deposit> deposits = new LinkedList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("depositLog.txt")));
			    String line;
			    while ((line = br.readLine()) != null) {
			       deposits.add(new Deposit(Double.parseDouble(line.split(" ")[0]), 
			    		   new Date(Long.parseLong(line.split(" ")[1]))));
			    }
			 br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return deposits;
	}
	
	/*funzione per rimuovere tutti i depositi più vecchi di 7 giorni rispetto
	 * alla data del deposito che si vuole registrare*/
	private void removeOlderLog(Date date) {
		List<Deposit> d = readDepositLog().stream().filter(deposit ->{
			Calendar depDate = Calendar.getInstance();
			depDate.setTime(deposit.getDate());
			depDate.add(Calendar.DAY_OF_MONTH, OLDER);
			Calendar today = Calendar.getInstance();
			today.setTime(date);
			return depDate.compareTo(today) >= 0;
		}).collect(Collectors.toList());
		try {
			BufferedWriter file = new BufferedWriter(new FileWriter(new File("depositLog.txt"), false));
			d.forEach(dep ->{
				try {
					file.write(String.valueOf(dep.getWeight())+ " " + String.valueOf(dep.getDate().getTime()) + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			file.close();
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}
	private void writeDepositLog(Double w, Long date) {	
		removeOlderLog(new Date(date));
		try {
			BufferedWriter file = new BufferedWriter(new FileWriter(new File("depositLog.txt"), true));
			file.write(w.toString() + " " + date.toString() + "\n");
			file.close();
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}
	private void handleNotAvailableState() {
		this.state = State.NOT_AVAILABLE;
		//readDepositLog().forEach(d -> System.out.println("deposito " + d.getWeight() + " data: " + d.getDate()));
	}

	private void updateWeight(RoutingContext routingContext) {
		JsonObject body = routingContext.getBodyAsJson();
		if(body != null) {
			depositFlag = false;
			this.availability = body.getDouble("availability");
			this.totalWeight = body.getDouble("totalWeight");
			this.nDeposit = body.getInteger("nDeposit");
			System.out.println("post esp AVL: " + availability + " TOT: "+ totalWeight + "nDep: " + nDeposit);
			this.lastDeposit = Optional.of(new Deposit(body.getDouble("value"),
					new Date(System.currentTimeMillis())));
			routingContext.response().setStatusCode(200).end();
		}else {
			sendError(400, routingContext.response());
		}
		if(this.lastDeposit.isPresent()) {
			writeDepositLog(lastDeposit.get().getWeight(), lastDeposit.get().getDate().getTime());
		}
		if(this.availability == 0.0) {
			handleNotAvailableState();
		}
	}
	
	private void getState(RoutingContext routingContext) {
		System.out.println("get state");
		String data = state == State.AVAILABLE && depositFlag ? "DEPOSIT" : state.toString();
		routingContext.response().setStatusCode(200)
		.putHeader("content-type", "text/plain")
		.end(data);
	}
	
	private void tokenRequest(RoutingContext routingContext) {
		System.out.println("token");
		if(state == State.AVAILABLE && !tokenTaken) {
			tokenTaken = true;
			routingContext.response().putHeader("content-type", "text/plain")
			.end("OK");
		}else {
			sendError(202, routingContext.response());
		}
	}
	
	private void setNewDeposit(RoutingContext routingContext) {
		depositFlag = true;
		tokenTaken = false;
		routingContext.response().setStatusCode(200).end();
	}
	
	private void handleState(RoutingContext routingContext) {
		System.out.println(routingContext.getBodyAsString() + " corpo post dash ");
		Boolean stato = Boolean.parseBoolean(routingContext.getBodyAsString());
		System.out.println(stato + " post dash");
		this.state = stato ? State.AVAILABLE : State.NOT_AVAILABLE;
		this.totalWeight = 0.0;
		this.availability = MAX_WEIGHT;
		this.nDeposit = 0;
		this.depositFlag = false;
		routingContext.response().setStatusCode(200).end();
	}
	
	
	private void getStatus(RoutingContext routingContext) {
		System.out.println("dash status");
		JsonObject json = new JsonObject();
		json.put("state", this.state != State.NOT_AVAILABLE);
		json.put("availability", this.availability);
		json.put("totalWeight", this.totalWeight);
		json.put("nDeposit", this.nDeposit);
		
		routingContext.response()
		.putHeader("content-type", "application/json")
		.end(json.encodePrettily());
	}
	
	private List<DailyLog> getDailyLog(){
		List<Deposit> l = readDepositLog();
		List<DailyLog> history = new ArrayList<>();
		if(l != null && l.size() != 0) {
			Date d = l.get(0).getDate();
			history.add(new DailyLog(d, 0.0, 0));
			l.forEach(dep -> {
				
				/*Usiamo Calendar perchè la classe Date, utilizzata per i Deposit,
				 * non ha dei metodi non deprecati per il confronto tra date, 
				 * inoltre, l'equals di Date considera anche i secondi rendendola 
				 * inutile nel nostro caso.*/
				Calendar cDep = Calendar.getInstance();
				cDep.setTime(dep.getDate());
				Calendar cHist = Calendar.getInstance();
				cHist.setTime(history.get(history.size()-1).getDate());
				
				if(cDep.get(Calendar.DAY_OF_MONTH) == cHist.get(Calendar.DAY_OF_MONTH)) {
					double w = history.get(history.size()-1).getTotalWeight();
					int n = history.get(history.size()-1).getnDeposit();
					w += dep.getWeight();
					n ++;
					history.get(history.size()-1).setTotalWeight(w);
					history.get(history.size()-1).setnDeposit(n);
					
				}else {
					history.add(new DailyLog(dep.getDate(), dep.getWeight(), 1));
				}
			});
		}
		return history;
	}
	private void getStatistics(RoutingContext routingContext) {
		
		JsonArray arr = new JsonArray();
		getDailyLog().forEach(d -> {
			
			/*formattiamo Date*/
			Calendar cDep = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"),Locale.ITALY);
			cDep.setTime(d.getDate());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			System.out.println(simpleDateFormat.format(cDep.getTime()) + " " + d.getnDeposit() + " " + d.getTotalWeight());
			
			JsonObject data = new JsonObject();
			data.put("date", simpleDateFormat.format(cDep.getTime()));
			data.put("totalWeight", d.getTotalWeight());
			data.put("nDep", d.getnDeposit());
			arr.add(data);
		}); 
		routingContext.response()
			.putHeader("content-type", "application/json")
			.end(arr.encodePrettily());
		
		System.out.println("statistics");
		
	}
		
	private void sendError(int statusCode, HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}

	private void log(String msg) {
		System.out.println("[DATA SERVICE] "+msg);
	}
	
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		DumpsterService service = new DumpsterService(8080);
		vertx.deployVerticle(service);
	}
	
	private enum State{
		AVAILABLE, NOT_AVAILABLE;
	}
}
