package com.example.prova1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.prova1.databinding.ActivityMainBinding;
import com.example.prova1.ui.AddLocationDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private TextView toolbarTitle;
    private Toolbar toolbar;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted != null && coarseLocationGranted) {
                    Snackbar.make(binding.getRoot(), "Permesso di localizzazione concesso!", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(binding.getRoot(), "Permesso negato. Le funzioni di mappa non saranno attive.", Snackbar.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbar_title);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.HomeFragment);
        topLevelDestinations.add(R.id.NotificationsFragment);
        topLevelDestinations.add(R.id.AllerteFragment);
        topLevelDestinations.add(R.id.ImpostazioniFragment);
        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
        
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            toolbarTitle.setText(destination.getLabel());
            
            int destinationId = destination.getId();
            if (destinationId == R.id.HomeFragment) {
                toolbar.setNavigationIcon(R.drawable.ic_home);
            } else if (destinationId == R.id.NotificationsFragment) {
                toolbar.setNavigationIcon(R.drawable.ic_notifications);
            } else if (destinationId == R.id.AllerteFragment) {
                toolbar.setNavigationIcon(R.drawable.ic_alert);
            } else if (destinationId == R.id.ImpostazioniFragment) {
                toolbar.setNavigationIcon(R.drawable.ic_settings);
            }

            invalidateOptionsMenu();
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                navController.navigate(R.id.HomeFragment);
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                navController.navigate(R.id.NotificationsFragment);
                return true;
            } else if (itemId == R.id.navigation_alerts) {
                navController.navigate(R.id.AllerteFragment);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                navController.navigate(R.id.ImpostazioniFragment);
                return true;
            }
            return false;
        });

        checkAndRequestLocationPermissions();
    }

    private void checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        NavDestination currentDestination = Navigation.findNavController(this, R.id.nav_host_fragment_content_main).getCurrentDestination();

        if (menu != null && currentDestination != null) {
            MenuItem addItem = menu.findItem(R.id.action_add_location);
            if (addItem != null) {
                addItem.setVisible(currentDestination.getId() == R.id.HomeFragment);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_location) {
            Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHostFragment != null) {
                Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).showAddLocationDialog();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
