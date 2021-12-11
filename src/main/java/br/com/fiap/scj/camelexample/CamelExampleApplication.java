package br.com.fiap.scj.camelexample;

import br.com.fiap.scj.camelexample.beans.MyBean;
import br.com.fiap.scj.camelexample.services.MyBeanService;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;

@SpringBootApplication
public class CamelExampleApplication {

	@Value("${server.port}")
	String serverPort;

	@Value("${fiap.api.path}")
	String contextPath;

	@Value("${camel.springboot.name}")
	String servletName;

	public static void main(String[] args) {
		SpringApplication.run(CamelExampleApplication.class, args);
	}

	@Bean
	ServletRegistrationBean servletRegistrationBean(){
		ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(), contextPath+ "/*");
		servlet.setName(servletName);
		return servlet;
	}

	@Component
	class RestApi extends RouteBuilder {

		@Override
		public void configure() throws Exception {
			final CamelContext context = new DefaultCamelContext();

			// http://localhost:8080/camel/api-doc
			restConfiguration()
					.contextPath(contextPath)
					.port(serverPort)
					.enableCORS(true)
					.apiContextPath("/api-doc")
					.apiProperty("api.title", "Camel Example Rest API")
					.apiProperty("api.version", "v1")
					.apiProperty("cors", "true")
					.apiContextRouteId("doc-api")
					.component("servlet")
					.bindingMode(RestBindingMode.json)
					.dataFormatProperty("prettyPrint", "true");

			rest("/api")
					.description("Teste Rest Service")
					.id("api-route")
					.post("/bean")
					.produces(MediaType.APPLICATION_JSON)
					.consumes(MediaType.APPLICATION_JSON)
					.bindingMode(RestBindingMode.auto)
					.type(MyBean.class)
					.enableCORS(true)
					.to("direct:remoteService");

			from("direct:remoteService")
					.routeId("direct-route")
					.tracing()
					.log(">>> ${body.id}")
					.log(">>> ${body.name}")
					.process(new Processor() {
						@Override
						public void process(Exchange exchange) throws Exception {
							MyBean bodyIn = (MyBean) exchange.getIn().getBody();

							MyBeanService.example(bodyIn);
							exchange.getIn().setBody(bodyIn);
						}
					})
					.log(">>> ${body.name}")
					.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));
		}
	}

}
