package googleMapSearchCoordinates;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.io.*;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Geocoder {
	
	private static String returnKey() {
		ResourceBundle rb = ResourceBundle.getBundle("googleGeocodingApi");
		return rb.getString("apiKey");
	}
	private static GeoApiContext context = new GeoApiContext.Builder()
			.apiKey(returnKey())
			.build();
	
	
	public static void main(String[] args) throws ApiException, InterruptedException, IOException{
		
		//ファイル読み込みで使用する３つのクラス
		FileInputStream fi = null;
		InputStreamReader is = null;
		BufferedReader br = null;
		// ファイル書き込みクラス
		PrintWriter pw = null;
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("CharacterCode: EX): Shift-JIS, UTF-8, ...");
		System.out.print("> ");
		String characterCode = scanner.nextLine();
		System.out.println("Select your search method (number) for GeoCoding.");
		System.out.println("1 : Use data CSV file contains BuildingName,BuildingAddress.");
		System.out.println("2 : Use data CSV file contains only BuildingAddress.");
		System.out.println("3 : Write 1 data directly on the console.");
		System.out.print("> ");
		String searchMethod = scanner.nextLine();
		if (searchMethod.equals("1")) {
			System.out.println("DataFile: EX): C:\\ ... ~.csv");
			System.out.print("> ");
			String readFile = scanner.nextLine();
			System.out.println("OutPutFile: EX): C:\\ ... ~.csv");
			System.out.print("> ");
			String targetFile = scanner.nextLine();
			
			// Fileクラスのオブジェクトを生成する
			File file = new File(targetFile);
			// PrintWriterクラスのオブジェクトを生成する
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), characterCode)));	// Shift-JIS, UTF-8
			
			//読み込みファイルのインスタンス生成, ファイル名と文字コードを指定する
			fi = new FileInputStream(readFile);
			is = new InputStreamReader(fi, characterCode);	// Shift-JIS, UTF-8
			br = new BufferedReader(is);
			//読み込み行
			String line;
			pw.println("BuildingName,Address,Latitude,Longitude");
			//1行ずつ読み込みを行う
			while ((line = br.readLine()) != null) {
				String[] dataAddress = line.split(",");
				/** Google Map GeoCoding */
				// insert BuildingName & Address
				String integratedData = dataAddress[1] + " " + dataAddress[0];
				GeocodingResult[] results = getResults(integratedData);
				if (results != null && results.length > 0) {
					// Use First Data (address data)
					LatLng latLng = results[0].geometry.location;
					// 緯度: latLng.lat, 経度: latLng.lng
					System.out.println("BuildingName: " + dataAddress[0] + ", Address: " + dataAddress[1] + ", Latitude: " + latLng.lat + ", Longitude: " + latLng.lng);
					pw.println(dataAddress[0] + "," + dataAddress[1] + "," + latLng.lat + "," + latLng.lng);
				}
			}
//			Thread.sleep(5);
			br.close();
			pw.close();
		}	else if (searchMethod.equals("2")) {
			System.out.println("DataFile: EX): C:\\ ... ~.csv");
			System.out.print("> ");
			String readFile = scanner.nextLine();
			System.out.println("OutPutFile: EX): C:\\ ... ~.csv");
			System.out.print("> ");
			String targetFile = scanner.nextLine();
			
			File file = new File(targetFile);
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), characterCode)));
			
			fi = new FileInputStream(readFile);
			is = new InputStreamReader(fi, characterCode);
			br = new BufferedReader(is);
			String line;
			pw.println("Address,Latitude,Longitude");
			while ((line = br.readLine()) != null) {
				String[] dataAddress = line.split(",");
				/** Google Map GeoCoding */
				GeocodingResult[] results = getResults(dataAddress[0]);
				if (results != null && results.length > 0) {
					LatLng latLng = results[0].geometry.location;
					System.out.println("Address: " + dataAddress[0] + ", Latitude: " + latLng.lat + ", Longitude: " + latLng.lng);
					pw.println(dataAddress[0] + "," + latLng.lat + "," + latLng.lng);
				}
			}
//			Thread.sleep(5);
			br.close();
			pw.close();
		}	else if (searchMethod.equals("3")) {
			System.out.println("Building Data: EX): \"Address\" or \"Address BuidlingName\"");
			System.out.print("> ");
			String buildingData = scanner.nextLine();
			/** Google Map GeoCoding */
			GeocodingResult[] results = getResults(buildingData);
			if (results != null && results.length > 0) {
				LatLng latLng = results[0].geometry.location;
				System.out.println("Data, " + "Latitude, " + "Longitude : ");
				System.out.println(buildingData + ", " + latLng.lat + "," + latLng.lng);
			}
			System.out.println("Close the console after 60 seconds. Copy your data within 60 seconds.");
			Thread.sleep(60000);
		}	else {
			System.out.println("Get out here!");
		}
	}
	
	
	/** Google Geocoding API getLatLng from Address */
	public static GeocodingResult[] getResults(String address) throws ApiException, InterruptedException, IOException {
		GeocodingApiRequest req = GeocodingApi.newRequest(context)
				.address(address)
//				.components(ComponentFilter.country("JP"))
				.language("ja");
		try {
			GeocodingResult[] results = req.await();
			if (results == null || results.length == 0) {
				// ZERO_RESULTSはresults.length==0の空配列がsuccessful扱いで返ってくるっぽい
				System.out.println("========== Zero results. ==========");
			}
			return results;
		}	catch (ApiException e) {
			// ZERO_RESULTS以外のApiExceptionはこっちで
			System.out.println("========== Geocode failed. ==========");
			e.printStackTrace();
			throw e;
		}	catch (IOException e) {
			System.out.println("========== IOException ==========");
			e.printStackTrace();
			throw e;
		}	catch (InterruptedException e) {
			System.out.println("========== InterruptedException ==========");
			e.printStackTrace();
			throw e;
		}
	}
}
