package com.sigma.androidnetworkproducts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sigma.androidnetworkproducts.adapters.ProductAdapter;
import com.sigma.androidnetworkproducts.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter();
        recyclerView.setAdapter(adapter);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        loadProducts();
    }

    private void loadProducts() {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://dummyjson.com/products");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                List<Product> products = parseProducts(jsonBuilder.toString());

                mainHandler.post(() -> adapter.setProducts(products));

            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(MainActivity.this,
                                "Ошибка загрузки данных",
                                Toast.LENGTH_LONG).show()
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private List<Product> parseProducts(String json) throws JSONException {
        List<Product> list = new ArrayList<>();

        JSONObject root = new JSONObject(json);
        JSONArray productsArray = root.getJSONArray("products");

        for (int i = 0; i < productsArray.length(); i++) {
            JSONObject obj = productsArray.getJSONObject(i);

            Product product = new Product();
            product.setId(obj.getInt("id"));
            product.setTitle(obj.getString("title"));
            product.setPrice(obj.getDouble("price"));
            product.setDescription(obj.getString("description"));
            product.setThumbnail(obj.getString("thumbnail"));

            list.add(product);
        }
        return list;
    }
}
