package com.curry.cognosDataSourceList;

import com.cognos.developer.schemas.bibus._3.*;
import com.curry.cognosCommon.CRNConnect;
import com.curry.cognosCommon.Logon;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.HashSet;

/**
 * Created by Jim on 11/18/2016.
 */
public class DataSourceLister {
	private static Logon sessionLogon;
	private CRNConnect connection = new CRNConnect();

	public DataSourceLister() {
		connection.connectToCognosServer();
		sessionLogon = new Logon();
		String output = "";

		while (!Logon.loggedIn(connection)) {
			output = sessionLogon.logon(connection);

			if (!Logon.loggedIn(connection)) {
				int retry =
						  JOptionPane.showConfirmDialog(
									 null,
									 "Login Failed. Please try again.",
									 "Login Failed",
									 JOptionPane.OK_CANCEL_OPTION);
				if (retry != JOptionPane.OK_OPTION) {
					System.exit(0);
				}
			}

		}

//		ContentManagerService_PortType cmService = connection.getCMService();
//
//		SearchPathMultipleObject dataSourceSearchPath = new SearchPathMultipleObject();
//		dataSourceSearchPath.set_value("CAMID(\":\")//dataSource");
//        searchPath.set_value("/configuration/dispatcher");

//        PropEnum props[] = {PropEnum.searchPath,
//                PropEnum.defaultName,
//                PropEnum.searchPath
//        };

//		PropEnum props[] = AllProperties.getProperties();
//
//		try {
//			BaseClass dataSources[] = cmService.query(dataSourceSearchPath, props, new Sort[]{}, new QueryOptions());
//			for (int i = 0; i < dataSources.length; i++) {
//				DataSource dataSource = (DataSource) dataSources[i];
//				System.out.println("\nData Source name: " + dataSource.getDefaultName().getValue());
//
//				SearchPathMultipleObject dataConnectionSearchPath = new SearchPathMultipleObject();
//				dataConnectionSearchPath.set_value(dataSource.getSearchPath().getValue() + "//dataSourceConnection");
//
//				BaseClass[] dataSourceConnections = cmService.query(dataConnectionSearchPath, props, new Sort[]{}, new QueryOptions());
//				for (int j = 0; j < dataSourceConnections.length; j++) {
//					DataSourceConnection dataSourceConnection = (DataSourceConnection) dataSourceConnections[j];
//					System.out.println("\tData Source Connection name: " + dataSourceConnection.getDefaultName().getValue());
//					System.out.println("\tData Source Connection string: " + dataSourceConnection.getConnectionString().getValue());
//
//					SearchPathMultipleObject dataSourceSignonSearchPath = new SearchPathMultipleObject();
//					dataSourceSignonSearchPath.set_value(dataSourceConnection.getSearchPath().getValue() + "//dataSourceSignon");
//					BaseClass[] dataSourceSignons = cmService.query(dataSourceSignonSearchPath, props, new Sort[]{}, new QueryOptions());
//					for (int k = 0; k < dataSourceSignons.length; k++) {
//						DataSourceSignon dataSourceSignon = (DataSourceSignon) dataSourceSignons[k];
//						System.out.println("\t\tData Source Signon name: " + dataSourceSignon.getDefaultName().getValue());
//					}
//				}
//			}
//		}
//		catch (RemoteException e) {
//			e.printStackTrace();
//		}


	}

	public static void main(String[] args) {
		DataSourceLister settingsDisplay = new DataSourceLister();
		settingsDisplay.listConnections();
	}

	private void listConnections() {
		//get location to put output file
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose where to save list of data sources");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				  "CSV & TXT", "csv", "txt");
		fileChooser.setFileFilter(filter);
		if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
			System.exit(0);
		}

		ContentManagerService_PortType cmService = connection.getCMService();

		//Get all the data sources in the content store
		SearchPathMultipleObject dataSourceSearchPath = new SearchPathMultipleObject();
		dataSourceSearchPath.set_value("CAMID(\":\")//dataSource");
		PropEnum props[] = AllProperties.getProperties();

		try {
			//Collection to hold unique list of connection types
			HashSet<String> connectionTypeSummary = new HashSet<String>();

			PrintWriter printWriter = new PrintWriter(fileChooser.getSelectedFile());
			printWriter.println("Data source Name\t"
					  + "Connection Name\t"
					  + "Connection Type\t"
					  + "Connection String"
			);

			BaseClass dataSources[] = cmService.query(dataSourceSearchPath, props, new Sort[]{}, new QueryOptions());
			for (int i = 0; i < dataSources.length; i++) {
				DataSource dataSource = (DataSource) dataSources[i];
				String dataSourceName = dataSource.getDefaultName().getValue();

				//Get the connections for the datasource
				SearchPathMultipleObject dataConnectionSearchPath = new SearchPathMultipleObject();
				dataConnectionSearchPath.set_value(dataSource.getSearchPath().getValue() + "//dataSourceConnection");

				BaseClass[] dataSourceConnections = cmService.query(dataConnectionSearchPath, props, new Sort[]{}, new QueryOptions());
				for (int j = 0; j < dataSourceConnections.length; j++) {
					DataSourceConnection dataSourceConnection = (DataSourceConnection) dataSourceConnections[j];
					String dataSourceConnectionName = dataSourceConnection.getDefaultName().getValue();
					String dataSourceConnectionString = dataSourceConnection.getConnectionString().getValue();
					String connectionType = getConnectionTypeFromConnectionString(dataSourceConnectionString);

					printWriter.println(dataSourceName + "\t"
							  +  dataSourceConnectionName + "\t"
							  + connectionType + "\t"
							  + dataSourceConnectionString + "\t"
					);			
					connectionTypeSummary.add(connectionType);
				}
			}
			for (String summaryConectionType : connectionTypeSummary) {
				printWriter.println("Summary\t"
						  + summaryConectionType + "\t");
			}

			printWriter.close();
			JOptionPane.showMessageDialog(
					null,
					"Done",
					"Finished",
					JOptionPane.INFORMATION_MESSAGE);

		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private String getConnectionTypeFromConnectionString(String connectionString) {
		StringBuilder connectionType = new StringBuilder();

		//Microsoft SQL Server (Native Client
		if (connectionString.toUpperCase().contains("PROVIDER=SQLNCLI11")) {
			connectionType.append("Microsoft SQL Server (Native Client)");
			if (connectionString.toUpperCase().contains("JDBC")) {
				connectionType.append(" w/ JDBC");
			}
		}
		//ORACLE
		else if (connectionString.toUpperCase().contains("LOCAL;OR;")) {
			connectionType.append("Oracle");
			if (connectionString.toUpperCase().contains("JDBC")) {
				connectionType.append(" w/ JDBC");
			}
		}
		//ODBC
		else if (connectionString.toUpperCase().contains("DSN=")) {
			connectionType.append("ODBC");
		}
		//SAP BW
		else if (connectionString.toUpperCase().contains("LOCAL;BW")) {
			connectionType.append("SAP BW");
		}
		//SAP ECC
		else if (connectionString.toUpperCase().contains("LOCAL;ERP-SAP")) {
			connectionType.append("SAP ERP");
		}
		//MS OLEDB
		else if (connectionString.toUpperCase().contains("INFO_TYPE=MS;PROVIDER=SQLOLEDB")) {
			connectionType.append("MS OLEDB");
		}
		//TM1
		else if (connectionString.toUpperCase().contains("LOCAL;TM")) {
			connectionType.append("TM1");
		}
		//Power Cube
		else if (connectionString.toUpperCase().contains("LOCAL;PC")) {
			connectionType.append("IBM Cognos PowerCube");
		}
		//Unknown
		else {
			connectionType.append("Unknown");
		}

		return connectionType.toString();
	}
}
