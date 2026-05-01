package com.example.reloop.ui.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;
import com.example.reloop.utils.LocationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddressFragment extends Fragment {

    private EditText etCountry, etCounty, etCity, etStreet, etPostcode;
    private Button btnUseGPS, btnSaveAddress, btnShowAddForm;
    private LinearLayout layoutAddressForm;
    private RecyclerView recyclerViewAddresses;

    private AddressAdapter adapter;
    private final List<String> savedAddressesList = new ArrayList<>();
    private DatabaseReference userAddressesRef;

    // Modern Permission Launcher
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI
        etCountry = view.findViewById(R.id.etCountry);
        etCounty = view.findViewById(R.id.etCounty);
        etCity = view.findViewById(R.id.etCity);
        etStreet = view.findViewById(R.id.etStreet);
        etPostcode = view.findViewById(R.id.etPostcode);
        btnUseGPS = view.findViewById(R.id.btnUseGPS);
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress);
        btnShowAddForm = view.findViewById(R.id.btnShowAddForm);
        layoutAddressForm = view.findViewById(R.id.layoutAddressForm);
        recyclerViewAddresses = view.findViewById(R.id.recyclerViewAddresses);

        // Setup RecyclerView
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AddressAdapter(savedAddressesList);
        recyclerViewAddresses.setAdapter(adapter);

        // Initial attempt to load list
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            userAddressesRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("addresses");
            loadSavedAddresses();
        }

        // Toggle Form Visibility
        btnShowAddForm.setOnClickListener(v -> {
            layoutAddressForm.setVisibility(View.VISIBLE);
            btnShowAddForm.setVisibility(View.GONE);
        });

        btnUseGPS.setOnClickListener(v -> checkLocationPermissions());
        btnSaveAddress.setOnClickListener(v -> saveAddressToDatabase());
    }

    private void loadSavedAddresses() {
        if (userAddressesRef == null) return;

        userAddressesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedAddressesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String street = dataSnapshot.child("street").getValue(String.class);
                    String city = dataSnapshot.child("city").getValue(String.class);
                    String county = dataSnapshot.child("county").getValue(String.class);
                    String country = dataSnapshot.child("country").getValue(String.class);
                    String postcode = dataSnapshot.child("postcode").getValue(String.class);

                    List<String> parts = new ArrayList<>();
                    if (street != null && !street.isEmpty()) parts.add(street);
                    if (city != null && !city.isEmpty()) parts.add(city);
                    if (county != null && !county.isEmpty()) parts.add(county);
                    if (country != null && !country.isEmpty()) parts.add(country);
                    if (postcode != null && !postcode.isEmpty()) parts.add(postcode);

                    String formattedAddress = android.text.TextUtils.join(", ", parts);
                    if (!formattedAddress.isEmpty()) {
                        savedAddressesList.add(formattedAddress);
                    }
                }
                adapter.notifyDataSetChanged();

                if (savedAddressesList.isEmpty()) {
                    layoutAddressForm.setVisibility(View.VISIBLE);
                    btnShowAddForm.setVisibility(View.GONE);
                } else {
                    layoutAddressForm.setVisibility(View.GONE);
                    btnShowAddForm.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AddressFragment", "Failed to read addresses", error.toException());
            }
        });
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        LocationUtils.getCurrentLocation(requireContext(), location -> {
            if (location != null) {
                reverseGeocode(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(getContext(), "Unable to retrieve location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reverseGeocode(double lat, double lng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                etCountry.setText(addr.getCountryName());
                etCounty.setText(addr.getAdminArea());

                String city = addr.getLocality();
                if (city == null || city.isEmpty()) city = addr.getSubAdminArea();
                etCity.setText(city != null ? city : "");

                etStreet.setText(addr.getThoroughfare());
                etPostcode.setText(addr.getPostalCode());

                Toast.makeText(getContext(), "Address updated from GPS", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("AddressFragment", "Geocoder failed", e);
        }
    }

    private void saveAddressToDatabase() {
        // Immediate feedback so you know the button actually clicked
        Toast.makeText(getContext(), "Processing...", Toast.LENGTH_SHORT).show();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "Error: You must be logged in to save.", Toast.LENGTH_LONG).show();
            return;
        }

        // FORCE the address list listener to connect if it failed during page load
        if (userAddressesRef == null) {
            userAddressesRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("addresses");
            loadSavedAddresses();
        }

        String country = etCountry.getText().toString().trim();
        String county = etCounty.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String postcode = etPostcode.getText().toString().trim();

        if (country.isEmpty() && county.isEmpty()) {
            Toast.makeText(getContext(), "Please enter at least a Country or a County/State", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("country", country);
        addressMap.put("county", county);
        addressMap.put("city", city);
        addressMap.put("street", street);
        addressMap.put("postcode", postcode);

        // OPTIMISTIC UPDATE: Hide the form and clear text instantly.
        layoutAddressForm.setVisibility(View.GONE);
        btnShowAddForm.setVisibility(View.VISIBLE);
        etCountry.setText("");
        etCounty.setText("");
        etCity.setText("");
        etStreet.setText("");
        etPostcode.setText("");

        // PUSH to Firebase (Using CompletionListener for better error handling)
        userAddressesRef.push().setValue(addressMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Log.e("AddressFragment", "Firebase Error", error.toException());
                    // If it fails, show the error
                    Toast.makeText(getContext(), "Cloud Save Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    // Success!
                    Toast.makeText(getContext(), "Address saved to Address Book!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // --- INNER CLASS FOR RECYCLERVIEW ADAPTER ---
    public static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
        private final List<String> addresses;

        public AddressAdapter(List<String> addresses) {
            this.addresses = addresses;
        }

        @NonNull
        @Override
        public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
            return new AddressViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
            holder.tvAddressSummary.setText(addresses.get(position));
        }

        @Override
        public int getItemCount() {
            return addresses.size();
        }

        public static class AddressViewHolder extends RecyclerView.ViewHolder {
            TextView tvAddressSummary;
            public AddressViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAddressSummary = itemView.findViewById(R.id.tvAddressSummary);
            }
        }
    }
}