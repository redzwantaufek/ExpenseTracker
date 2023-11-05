package com.example.expensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.expensetracker.databinding.ActivityMainBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private ExpenseAdapter expenseAdapter;
    private long income=0, expense=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        expenseAdapter = new ExpenseAdapter(this);
        binding.recycler.setAdapter(expenseAdapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);

        binding.addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("type","Income");
                startActivity(intent);
            }
        });

        binding.addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("type","Expense");
                startActivity(intent);
            }
        });

         Button faqButton = findViewById(R.id.btn_faq);
        faqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent faqIntent = new Intent(MainActivity.this, FAQActivity.class);
                startActivity(faqIntent);
            }
        });
    }

           

    /*@Override
    protected void onStart() {
        super.onStart();
        ProgressDialog dialog = new ProgressDialog(this);
        progrssDialog.setTitle("Please");
        progrssDialog.setMessage("Wait");
        progrssDialog.setCancelable(false);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance()
                    .signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressDialog.cancel();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.cancel();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        income=0;expense=0;
        getData();
    }

    private void getData() {
        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                          expenseAdapter.clear();
                        List<DocumentSnapshot> dsList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds:dsList) {
                            ExpenseModel expenseModel = ds.toObject(ExpenseModel.class);
                            if (expenseModel.getType().equals("Income")){
                                income+=expenseModel.getAmount();
                            }else{
                                expense+=expenseModel.getAmount();
                            }
                            expenseAdapter.add(expenseModel);
                        }
                        setUpGraph();
                    }
                });
    }

    private void setUpGraph() {
        List<PieEntry> pieEntryList = new ArrayList<>();
        List<Integer>colorList = new ArrayList<>();
        if (income!=0){
            pieEntryList.add(new PieEntry(income,"Income"));
            colorList.add(getResources().getColor(R.color.teal_700));
        }
        if (expense!=0){
            pieEntryList.add(new PieEntry(expense,"Expense"));
            colorList.add(getResources().getColor(R.color.red));
        }
        PieDataSet pieDataSet = new PieDataSet(pieEntryList, "Balance: RM" + (income-expense));
        pieDataSet.setColors(colorList);
        pieDataSet.setValueTextColor(getResources().getColor(R.color.white));
        PieData pieDat= new PieData(pieDataSet);
    
        binding.pieChart.setData(pieDat);
        binding.pieChart.invalidate();
    }

    public void onClick(ExpenseModel expenseModel){
        Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
        intent.putExtra("model", expenseModel);
        startActivity(intent);
    }
}