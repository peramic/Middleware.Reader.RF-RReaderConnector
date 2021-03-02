package havis.middleware.reader.rf_r.osgi;

import havis.middleware.ale.reader.ReaderConnector;
import havis.middleware.ale.reader.ReaderConnectorProcess;
import havis.middleware.ale.reader.ReaderConnectorRemoteServer;
import havis.middleware.reader.rf_r.RF_RReaderConnectorClient;
import havis.middleware.reader.rf_r.hywear.HyWearRF_RReaderConnectorServer;
import havis.middleware.reader.rf_r400.HostRF_R400ReaderConnectorServer;
import havis.middleware.reader.rf_r400.NotificationRF_R400ReaderConnectorServer;
import havis.middleware.reader.rf_r500.HostRF_R500ReaderConnectorServer;
import havis.middleware.reader.rf_r500.NotificationRF_R500ReaderConnectorServer;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private static final Logger log = Logger.getLogger(Activator.class
			.getName());

	private static final String NAME = "name";
	private static final String[] VALUES_400 = { "RF-R400", "LRU1002", "FEIG.LRU1002" };
	private static final String[] VALUES_400_STREAM = { "RF-R400|Stream" };
	private static final String[] VALUES_500 = { "RF-R500", "LRU3000", "LRU3500", "FEIG.LRU3x00" };
	private static final String[] VALUES_500_STREAM = { "RF-R500|Stream" };
	private static final String[] VALUES_HYWEAR_SCAN = { "FEIG.HyWEAR|Scan" };

	private List<ServiceRegistration<?>> registrations = new CopyOnWriteArrayList<>();

	@Override
	public void start(final BundleContext context) throws Exception {
		for (String value : VALUES_400)
			register(context, value, HostRF_R400ReaderConnectorServer.class);
		for (String value : VALUES_500)
			register(context, value, HostRF_R500ReaderConnectorServer.class);
		for (String value : VALUES_500_STREAM)
			register(context, value, NotificationRF_R500ReaderConnectorServer.class);
		for (String value : VALUES_400_STREAM)
			register(context, value, NotificationRF_R400ReaderConnectorServer.class);
		for (String value : VALUES_HYWEAR_SCAN)
			register(context, value, HyWearRF_RReaderConnectorServer.class);
	}

	private void register(final BundleContext context, final String name,
			final Class<?> clazz) {
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put(NAME, name);

		log.log(Level.FINE, "Register service {0} ({1}={2})", new Object[] {
				clazz.getName(), NAME, name });
		registrations.add(context.registerService(
				ReaderConnector.class.getName(),
				new PrototypeServiceFactory<ReaderConnector>() {
					@Override
					public ReaderConnector getService(Bundle bundle,
							ServiceRegistration<ReaderConnector> registration) {
						try {
							if (ReaderConnectorRemoteServer.class
									.isAssignableFrom(clazz)) {
								// host in a new process
								return new RF_RReaderConnectorClient(
										new ReaderConnectorProcess() {
											private ServiceObjects<Observable> serviceObjects;
											{
												Collection<ServiceReference<Observable>> references = context
														.getServiceReferences(
																Observable.class,
																"(class="
																		+ clazz.getName()
																		+ ")");
												for (ServiceReference<Observable> reference : references) {
													serviceObjects = context
															.getServiceObjects(reference);
													break;
												}
											}

											@Override
											public void unget(
													Observable observable) {
												if (serviceObjects != null) {
													serviceObjects
															.ungetService(observable);
												} else {
													throw new IllegalStateException(
															"Failed to unget process for class "
																	+ clazz.getName());
												}
											}

											@Override
											public Observable get() {
												if (serviceObjects != null) {
													return serviceObjects
															.getService();
												}
												throw new IllegalStateException(
														"Failed to get process for class "
																+ clazz.getName());
											}
										});
							} else {
								return (ReaderConnector) clazz.newInstance();
							}
						} catch (Exception e) {
							log.log(Level.SEVERE, "Failed to register " + name,
									e);
							return null;
						}
					}

					@Override
					public void ungetService(Bundle bundle,
							ServiceRegistration<ReaderConnector> registration,
							ReaderConnector service) {
					}
				}, properties));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		for (ServiceRegistration<?> registration : this.registrations) {
			registration.unregister();
		}
	}
}