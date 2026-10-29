package com.example.excessbaggagefee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etWeight, etRate;
    private TextInputLayout tilWeight, tilRate;
    private Spinner spinnerClass;
    private Button btnCalculate;
    private TextView tvExcessWeight, tvExcessCharge, tvTotalPayable, tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar (title bar + share/about menu)
        setSupportActionBar(findViewById(R.id.toolbar));

        // Connect UI variables to XML components
        etWeight = findViewById(R.id.etWeight);
        etRate = findViewById(R.id.etRate);
        tilWeight = findViewById(R.id.tilWeight);
        tilRate = findViewById(R.id.tilRate);
        spinnerClass = findViewById(R.id.spinnerClass);
        btnCalculate = findViewById(R.id.btnCalculate);
        tvExcessWeight = findViewById(R.id.tvExcessWeight);
        tvExcessCharge = findViewById(R.id.tvExcessCharge);
        tvTotalPayable = findViewById(R.id.tvTotalPayable);
        tvMessage = findViewById(R.id.tvMessage);

        // Setup Dropdown for Ticket Class
        String[] ticketClasses = {"Economy (20 kg free)", "Business (40 kg free)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ticketClasses);
        spinnerClass.setAdapter(adapter);

        // Remove error message once user starts typing again
        clearErrorOnType(etWeight, tilWeight);
        clearErrorOnType(etRate, tilRate);

        // Calculate Button click listener
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateFee();
            }
        });
    }

    private void clearErrorOnType(EditText editText, final TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void calculateFee() {
        String weightInput = etWeight.getText().toString().trim();
        String rateInput = etRate.getText().toString().trim();

        // Validation: empty boxes
        if (weightInput.isEmpty()) {
            tilWeight.setError("Please enter baggage weight! 🧳");
            return;
        }
        if (rateInput.isEmpty()) {
            tilRate.setError("Please enter rate per kg! 💸");
            return;
        }

        // Validation: invalid numbers (e.g. just ".")
        double baggageWeight, ratePerKg;
        try {
            baggageWeight = Double.parseDouble(weightInput);
        } catch (NumberFormatException e) {
            tilWeight.setError("Enter a valid number, e.g. 25.5");
            return;
        }
        try {
            ratePerKg = Double.parseDouble(rateInput);
        } catch (NumberFormatException e) {
            tilRate.setError("Enter a valid number, e.g. 15");
            return;
        }

        if (baggageWeight < 0 || ratePerKg < 0) {
            Toast.makeText(this, "Values cannot be negative 💕", Toast.LENGTH_SHORT).show();
            return;
        }

        // Allowance: 20kg for Economy (index 0), 40kg for Business (index 1)
        double freeAllowance = (spinnerClass.getSelectedItemPosition() == 0) ? 20.0 : 40.0;

        // Calculate Excess Weight
        double excessWeight = baggageWeight - freeAllowance;
        if (excessWeight < 0) {
            excessWeight = 0.0; // Never negative
        }

        // Calculate Charges
        double excessCharge = excessWeight * ratePerKg;
        double totalPayable = excessCharge + (excessCharge * 0.08); // 8% SST

        // Display results
        tvExcessWeight.setText(String.format(Locale.getDefault(), "%.1f kg", excessWeight));
        tvExcessCharge.setText(String.format(Locale.getDefault(), "RM %.2f", excessCharge));
        tvTotalPayable.setText(String.format(Locale.getDefault(), "RM %.2f", totalPayable));

        // Friendly message
        if (excessWeight == 0.0) {
            tvMessage.setText(String.format(Locale.getDefault(),
                    "🎉 Yay! No excess baggage. You're within your %.0f kg free allowance!",
                    freeAllowance));
        } else {
            tvMessage.setText(String.format(Locale.getDefault(),
                    "🧳 Your bag is %.1f kg over the %.0f kg free allowance.",
                    excessWeight, freeAllowance));
        }
    }

    // ---------- Toolbar menu (Share + About) ----------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.share_text, getString(R.string.app_url)));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser)));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}