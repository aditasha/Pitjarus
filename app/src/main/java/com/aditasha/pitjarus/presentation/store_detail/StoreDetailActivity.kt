package com.aditasha.pitjarus.presentation.store_detail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.aditasha.pitjarus.R
import com.aditasha.pitjarus.databinding.ActivityStoreDetailBinding
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import com.aditasha.pitjarus.presentation.store_visit.StoreVisitActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class StoreDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreDetailBinding
    private val detailViewModel: StoreDetailViewModel by viewModels()
    private lateinit var storeEntity: StoreEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoreDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launchWhenStarted {
            detailViewModel.storeResult.collectLatest { result ->
                binding.loading.isVisible = result is Result.Loading
                if (result is Result.Error) showError(result.localizedMessage)
                if (result is Result.Success) {
                    storeEntity = result.data
                    binding.apply {
                        val bitmap = BitmapFactory.decodeFile(storeEntity.picture)
                        picture.setImageBitmap(bitmap)
                        storeId.text =
                            resources.getString(R.string.store_id, storeEntity.storeId.toString())
                        storeName.text = storeEntity.storeName.toString()
                        storeSubchannel.text = resources.getString(
                            R.string.sub_channel,
                            storeEntity.subchannelName.toString()
                        )
                        storeChannel.text = resources.getString(
                            R.string.channel,
                            storeEntity.channelName.toString()
                        )
                        storeAreaRegion.text = resources.getString(
                            R.string.store_area_region,
                            storeEntity.areaName.toString(),
                            storeEntity.regionName.toString()
                        )
                    }
                }
            }
        }

        val roomId = intent.extras?.getInt(StoreVisitActivity.ROOM_ID)
        if (roomId != null) {
            detailViewModel.fetchStore(roomId)
        }

        binding.finish.setOnClickListener { finish() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showError(localizedMessage: String) {
        val snack = Snackbar.make(
            binding.root,
            localizedMessage,
            Snackbar.LENGTH_LONG
        )
        val params =
            snack.view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        snack.setTextMaxLines(10)
        snack.show()
    }
}