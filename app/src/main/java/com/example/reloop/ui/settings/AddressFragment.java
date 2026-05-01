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
import android.widget.ImageButton;
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
import com.example.reloop.models.AddressModel;
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
    private final List<AddressModel> savedAddressesList = new ArrayList<>();
    private DatabaseReference userAddressesRef;

    // Tracks if we are editing an existing address or creating a new one
    private String editingAddressKey = null;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
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

        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with click listeners for Edit and Delete
        adapter = new AddressAdapter(savedAddressesList, new AddressAdapter.OnAddressClickListener() {
            @Override
            public void onEditClick(AddressModel address) {
                enterEditMode(address);
            }

            @Override
            public void onDeleteClick(AddressModel address) {
                deleteAddressFromFirebase(address);
            }
        });
        recyclerViewAddresses.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            userAddressesRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("addresses");
            loadSavedAddresses();
        }

        btnShowAddForm.setOnClickListener(v -> showForm(true));
        btnUseGPS.setOnClickListener(v -> checkLocationPermissions());
        btnSaveAddress.setOnClickListener(v -> saveOrUpdateAddress());
    }

    private void loadSavedAddresses() {
        if (userAddressesRef == null) return;

        userAddressesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedAddressesList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AddressModel address = ds.getValue(AddressModel.class);
                    if (address != null) {
                        address.key = ds.getKey();
                        savedAddressesList.add(address);
                    }
                }
                adapter.notifyDataSetChanged();
                showForm(savedAddressesList.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AddressFragment", "Firebase Error", error.toException());
            }
        });
    }

    private void enterEditMode(AddressModel address) {
        editingAddressKey = address.key;
        etCountry.setText(address.country);
        etCounty.setText(address.county);
        etCity.setText(address.city);
        etStreet.setText(address.street);
        etPostcode.setText(address.postcode);

        btnSaveAddress.setText("Update Address");
        showForm(true);
        Toast.makeText(getContext(), "Edit Mode", Toast.LENGTH_SHORT).show();
    }

    private void deleteAddressFromFirebase(AddressModel address) {
        userAddressesRef.child(address.key).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Address deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
    }

    private void saveOrUpdateAddress() {
        String country = etCountry.getText().toString().trim();
        String county = etCounty.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String postcode = etPostcode.getText().toString().trim();

        if (country.isEmpty() || city.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in City and Country", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("country", country);
        addressMap.put("county", county);
        addressMap.put("city", city);
        addressMap.put("street", street);
        addressMap.put("postcode", postcode);

        DatabaseReference targetRef;
        if (editingAddressKey != null) {
            targetRef = userAddressesRef.child(editingAddressKey); // Edit existing[cite: 1]
        } else {
            targetRef = userAddressesRef.push(); // Create new[cite: 1]
        }

        targetRef.setValue(addressMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Address saved successfully!", Toast.LENGTH_SHORT).show();
                resetForm();
            } else {
                Toast.makeText(getContext(), "Save failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetForm() {
        editingAddressKey = null;
        etCountry.setText("");
        etCounty.setText("");
        etCity.setText("");
        etStreet.setText("");
        etPostcode.setText("");
        btnSaveAddress.setText("Save Address");
        showForm(false);
    }

    private void showForm(boolean isVisible) {
        layoutAddressForm.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        btnShowAddForm.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        LocationUtils.getCurrentLocation(requireContext(), location -> {
            if (location != null) reverseGeocode(location.getLatitude(), location.getLongitude());
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
                etCity.setText(addr.getLocality() != null ? addr.getLocality() : addr.getSubAdminArea());
                etStreet.setText(addr.getThoroughfare());
                etPostcode.setText(addr.getPostalCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Adapter Implementation ---
    public static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
        private final List<AddressModel> addressList;
        private final OnAddressClickListener listener;

        public interface OnAddressClickListener {
            void onEditClick(AddressModel address);
            void onDeleteClick(AddressModel address);
        }

        public AddressAdapter(List<AddressModel> addressList, OnAddressClickListener listener) {
            this.addressList = addressList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
            return new AddressViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
            AddressModel address = addressList.get(position);
            holder.tvAddressSummary.setText(address.getFullAddress());
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(address));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(address));
        }

        @Override
        public int getItemCount() {
            return addressList.size();
        }

        public static class AddressViewHolder extends RecyclerView.ViewHolder {
            TextView tvAddressSummary;
            ImageButton btnEdit, btnDelete;
            public AddressViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAddressSummary = itemView.findViewById(R.id.tvAddressSummary);
                btnEdit = itemView.findViewById(R.id.btnEditAddress);
                btnDelete = itemView.findViewById(R.id.btnDeleteAddress);
            }
        }
    }
}
