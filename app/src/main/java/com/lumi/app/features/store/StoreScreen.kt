package com.lumi.app.features.store

import com.lumi.app.core.components.LumiEmoji
import com.lumi.app.mascot.MascotCelebration
import com.lumi.app.mascot.LumiAnimations
import com.lumi.app.mascot.MascotImage
import com.lumi.app.core.components.LumiIcon
import com.lumi.app.core.components.LumiIconKind
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.core.components.SkeletonList
import com.lumi.app.data.models.store.StoreItemResponse
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

private data class StoreOffer(
    val emoji: String,
    val title: String,
    val description: String,
    val price: Int? = null,
    val status: String? = null,
    // Id del artículo en el catálogo del servidor (null en ofertas informativas)
    val itemId: String? = null
)

private data class GemPack(
    val emoji: String,
    val amount: String,
    val price: String
)

@Composable
fun StoreScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

    val storeViewModel: StoreViewModel = viewModel(
        factory = StoreViewModelFactory(
            LumiApp.container.storeRepository,
            LumiApp.container.progressRepository
        )
    )
    val store by storeViewModel.data.collectAsState()
    val buying by storeViewModel.buying.collectAsState()
    val storeMessage by storeViewModel.message.collectAsState()

    LaunchedEffect(Unit) {
        storeViewModel.load()
    }

    LaunchedEffect(storeMessage) {
        storeMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            storeViewModel.consumeMessage()
        }
    }

    val gems = store.gems

    var selectedOffer by remember { mutableStateOf<StoreOffer?>(null) }
    val purchased by storeViewModel.purchased.collectAsState()
    var showNoGems by remember { mutableStateOf(false) }

    purchased?.let {
        MascotCelebration(
            animationRes = LumiAnimations.PURCHASE,
            title = it,
            onDismiss = { storeViewModel.consumePurchased() }
        )
    }

    if (showNoGems) {
        MascotCelebration(
            animationRes = LumiAnimations.NO_GEMS,
            title = stringResource(R.string.sin_gemas_suficientes),
            onDismiss = { showNoGems = false }
        )
    }
    var promoCode by remember { mutableStateOf("") }
    var promoMessage by remember { mutableStateOf<String?>(null) }

    val specialOffers = listOf(
        StoreOffer(
            "🌅",
            AppText.get(R.string.recompensa_de_estudio_matutino),
            AppText.get(R.string.completa_una_leccion_entre_las),
            status = "DISPONIBLE"
        ),
        StoreOffer(
            "🌙",
            AppText.get(R.string.recompensa_de_estudio_nocturno),
            AppText.get(R.string.completa_una_leccion_entre_las_2),
            status = "DISPONIBLE"
        )
    )

    // Catálogo real del servidor, separado por tipo de artículo
    val catalogOffers = store.items.map { item ->
        val owned = store.inventory[item.id] ?: 0
        StoreOffer(
            emoji = storeEmoji(item),
            title = item.name,
            description = item.description.orEmpty(),
            price = item.priceGems,
            status = if (owned > 0) stringResource(R.string.en_inventario, owned) else null,
            itemId = item.id
        ) to item
    }

    val streakItems = catalogOffers.filter { isStreakItem(it.second) }.map { it.first }
    val boosters = catalogOffers.filterNot { isStreakItem(it.second) }.map { it.first }

    val gemPacks = listOf(
        GemPack("lumi:GemsPile", "1,200", "$119.00"),
        GemPack("lumi:GemsBag", "3,000", "$235.00"),
        GemPack("lumi:GemsChest", "6,500", "$469.00")
    )

    Scaffold(
        containerColor = LumiColors.Cream,
        bottomBar = {
            LumiBottomBar(
                selectedRoute = "store",
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LumiColors.Cream)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ENCABEZADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.tienda),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark,
                    modifier = Modifier.weight(1f)
                )
                MascotImage(LumiAnimations.STORE, 84.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LumiIcon(LumiIconKind.Gem, size = 24.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = gems.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark
                    )
                }
            }

            // OFERTAS ESPECIALES
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading(stringResource(R.string.ofertas_especiales))
                specialOffers.forEach { offer ->
                    SpecialOfferCard(offer = offer)
                }
            }

            // DÍAS DE RACHA
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading(stringResource(R.string.dias_de_racha_2))
                if (streakItems.isEmpty()) EmptyStoreText(store.loaded)
                streakItems.forEach { offer ->
                    StoreItemRow(
                        offer = offer,
                        onClick = { selectedOffer = offer }
                    )
                }
            }

            // GEMAS
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading(stringResource(R.string.gemas))
                gemPacks.forEach { pack ->
                    val packTitle = stringResource(R.string.gemas_2, pack.amount)
                    val packDescription = stringResource(R.string.paquete_de_gemas_por_la, pack.amount, pack.price)
                    GemPackRow(
                        pack = pack,
                        onClick = {
                            selectedOffer = StoreOffer(
                                emoji = pack.emoji,
                                title = packTitle,
                                description = packDescription
                            )
                        }
                    )
                }
            }

            // POTENCIADORES
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading(stringResource(R.string.potenciadores))
                if (boosters.isEmpty()) EmptyStoreText(store.loaded)
                boosters.forEach { offer ->
                    StoreItemRow(
                        offer = offer,
                        onClick = { selectedOffer = offer }
                    )
                }
            }

            // CÓDIGO PROMOCIONAL
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading(stringResource(R.string.codigo_promocional))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = LumiColors.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LumiEmoji("🎟️", size = 42.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                AppText.get(R.string.ingresa_un_codigo_promocional),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = LumiColors.Dark
                            )
                            Text(
                                AppText.get(R.string.canjea_un_codigo_y_recibe),
                                fontSize = 11.sp,
                                color = LumiColors.Dark.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = promoCode,
                                onValueChange = {
                                    promoCode = it
                                    promoMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text(stringResource(R.string.codigo_promocional)) },
                                shape = RoundedCornerShape(12.dp)
                            )
                            TextButton(
                                onClick = {
                                    promoMessage = if (promoCode.isBlank()) {
                                        AppText.get(R.string.escribe_un_codigo_para_continuar)
                                    } else {
                                        AppText.get(R.string.el_canje_de_codigos_estara)
                                    }
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "CANJEAR",
                                    color = LumiColors.YellowStrong,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            promoMessage?.let {
                                Text(
                                    text = it,
                                    fontSize = 11.sp,
                                    color = LumiColors.Dark.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    selectedOffer?.let { offer ->
        AlertDialog(
            onDismissRequest = { selectedOffer = null },
            containerColor = LumiColors.White,
            title = {
                Text(offer.title, color = LumiColors.Dark, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = buildString {
                        append(offer.description)
                        offer.price?.let { append(AppText.get(R.string.n_nprecio_gemas, it)) }
                    },
                    color = LumiColors.Dark
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val purchasable = store.items.firstOrNull { it.id == offer.itemId }
                        if (purchasable != null) {
                            if (gems >= purchasable.priceGems) {
                                storeViewModel.purchase(purchasable)
                            } else {
                                showNoGems = true
                            }
                        }
                        selectedOffer = null
                    },
                    enabled = offer.itemId == null || (!buying && offer.price != null)
                ) {
                    Text(
                        if (offer.itemId == null) stringResource(R.string.entendido) else stringResource(R.string.canjear),
                        color = LumiColors.YellowStrong,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOffer = null }) {
                    Text(stringResource(R.string.cerrar), color = LumiColors.Dark)
                }
            }
        )
    }
}

@Composable
private fun StoreSectionHeading(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = LumiColors.Dark
        )
        subtitle?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = LumiColors.Dark.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SpecialOfferCard(offer: StoreOffer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LumiColors.Cream),
                contentAlignment = Alignment.Center
            ) {
                LumiEmoji(offer.emoji, size = 34.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    offer.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    offer.description,
                    fontSize = 11.sp,
                    color = LumiColors.Dark.copy(alpha = 0.72f)
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    offer.status ?: "DISPONIBLE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.YellowStrong
                )
            }
        }
    }
}

@Composable
private fun StoreItemRow(
    offer: StoreOffer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LumiColors.Cream),
                contentAlignment = Alignment.Center
            ) {
                LumiEmoji(offer.emoji, size = 36.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    offer.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    offer.description,
                    fontSize = 11.sp,
                    color = LumiColors.Dark.copy(alpha = 0.72f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    offer.status ?: (offer.price?.let { stringResource(R.string.gemas_2, it) } ?: "DISPONIBLE"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.YellowStrong
                )
            }
        }
    }
}

@Composable
private fun GemPackRow(
    pack: GemPack,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(LumiColors.Cream),
                contentAlignment = Alignment.Center
            ) {
                LumiEmoji(pack.emoji, size = 34.dp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = pack.amount,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark
            )
            Text(
                text = pack.price,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LumiColors.YellowStrong
            )
        }
    }
}

internal fun isStreakItem(item: StoreItemResponse): Boolean {
    val text = "${item.type.orEmpty()} ${item.name}".lowercase()
    return "streak" in text || "racha" in text || "protector" in text
}

private fun storeEmoji(item: StoreItemResponse): String {
    val text = "${item.type.orEmpty()} ${item.name}".lowercase()
    return when {
        isStreakItem(item) -> "🛡️"
        "xp" in text || "boost" in text || "multiplic" in text -> "🧪"
        "time" in text || "tiempo" in text -> "⏱️"
        else -> "🎁"
    }
}

@Composable
private fun EmptyStoreText(loaded: Boolean) {
    if (loaded) {
        Text(
            text = stringResource(R.string.no_hay_articulos_disponibles_por),
            fontSize = 13.sp,
            color = LumiColors.Dark.copy(alpha = 0.65f)
        )
    } else {
        SkeletonList(count = 2)
    }
}
