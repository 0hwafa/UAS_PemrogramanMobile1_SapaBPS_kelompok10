package com.sapabps

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sapabps.data.local.AppDatabase
import com.sapabps.data.repository.GuestBookRepository
import com.sapabps.security.AuthGuardActivity
import com.sapabps.ui.guestbook.CreateGuestBookActivity
import com.sapabps.ui.guestbook.DetailGuestBookActivity
import com.sapabps.ui.guestbook.ScanQrActivity
import com.sapabps.ui.guestbook.GuestBookAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AuthGuardActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvTotalQueue: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var fabScanQr: FloatingActionButton
    private lateinit var toolbar: Toolbar

    private lateinit var guestBookRepository: GuestBookRepository
    private lateinit var adapter: GuestBookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionManager.isLoggedIn()) return
        
        setContentView(R.layout.activity_main)

        val db = AppDatabase.getDatabase(this)
        guestBookRepository = GuestBookRepository(db.guestBookDao())

        tvWelcome = findViewById(R.id.tvWelcome)
        tvRole = findViewById(R.id.tvRole)
        tvTotalQueue = findViewById(R.id.tvTotalQueue)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.fab)
        fabScanQr = findViewById(R.id.fabScanQr)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val user = currentUser()
        if (user != null) {
            tvWelcome.text = getString(R.string.welcome_message, user.fullName)
            tvRole.text = "Role: ${user.role}"

            // Show scan QR FAB only for admin
            if (user.role == "admin") {
                fabScanQr.visibility = View.VISIBLE
                fabScanQr.setOnClickListener {
                    startActivity(Intent(this, ScanQrActivity::class.java))
                }
            }
        }

        adapter = GuestBookAdapter { guestBook ->
            val intent = Intent(this, DetailGuestBookActivity::class.java)
            intent.putExtra("GUESTBOOK_ID", guestBook.id)
            startActivity(intent)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(this, CreateGuestBookActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadQueueHistory()
    }

    private fun loadQueueHistory() {
        val user = currentUser() ?: return
        
        lifecycleScope.launch(Dispatchers.IO) {
            val history = guestBookRepository.getQueueHistory(user.id, user.role)
            
            withContext(Dispatchers.Main) {
                tvTotalQueue.text = getString(R.string.total_queue, history.size)
                adapter.submitList(history)
                if (history.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}