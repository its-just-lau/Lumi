package com.lumi.app.features.store

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
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.ui.theme.LumiColors

private data class StoreOffer(
    val emoji: String,
    val title: String,
    val description: String,
    val price: Int? = null,
    val status: String? = null
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
    // Datos de demostración. Después se conectarán al inventario y al backend.
    var gems by remember { mutableIntStateOf(853) }
    var equippedProtector by remember { mutableIntStateOf(2) }
    var purchasedItems by remember { mutableStateOf(setOf<String>()) }
    var selectedOffer by remember { mutableStateOf<StoreOffer?>(null) }
    var promoCode by remember { mutableStateOf("") }
    var promoMessage by remember { mutableStateOf<String?>(null) }

    val specialOffers = listOf(
        StoreOffer(
            "🌅",
            "Recompensa de estudio matutino",
            "Completa una lección entre las 6 a.m. y el mediodía para obtener esta recompensa.",
            status = "DISPONIBLE"
        ),
        StoreOffer(
            "🌙",
            "Recompensa de estudio nocturno",
            "Completa una lección entre las 6 p.m. y la medianoche para obtener esta recompensa.",
            status = "DISPONIBLE"
        )
    )

    val streakItems = listOf(
        StoreOffer(
            "🛡️",
            "Protector de racha",
            "Protege tu racha si no puedes estudiar por un día. Puedes equipar hasta 2 a la vez.",
            450,
            "$equippedProtector / 2 EQUIPADOS"
        )
    )

    val boosters = listOf(
        StoreOffer(
            "🧪",
            "Multiplicador de XP",
            "Gana el triple de XP en tus lecciones.",
            500,
            "RECIBE TU RECOMPENSA"
        ),
        StoreOffer(
            "⏱️",
            "Tiempo extra",
            "Agrega tiempo adicional para completar tus desafíos.",
            450
        )
    )

    val gemPacks = listOf(
        GemPack("💰", "1,200", "$119.00"),
        GemPack("💎", "3,000", "$235.00"),
        GemPack("🪙", "6,500", "$469.00")
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
                    text = "Tienda",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 20.sp)
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
                StoreSectionHeading("Ofertas especiales")
                specialOffers.forEach { offer ->
                    SpecialOfferCard(offer = offer)
                }
            }

            // DÍAS DE RACHA
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading("Días de racha")
                streakItems.forEach { offer ->
                    StoreItemRow(
                        offer = offer,
                        onClick = { selectedOffer = offer }
                    )
                }
            }

            // GEMAS
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading("Gemas")
                gemPacks.forEach { pack ->
                    GemPackRow(
                        pack = pack,
                        onClick = {
                            selectedOffer = StoreOffer(
                                emoji = pack.emoji,
                                title = "${pack.amount} gemas",
                                description = "Paquete de ${pack.amount} gemas por ${pack.price}. La compra real se habilitará más adelante."
                            )
                        }
                    )
                }
            }

            // POTENCIADORES
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading("Potenciadores")
                boosters.forEach { offer ->
                    StoreItemRow(
                        offer = offer,
                        onClick = { selectedOffer = offer }
                    )
                }
            }

            // CÓDIGO PROMOCIONAL
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreSectionHeading("Código promocional")
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
                        Text("🎟️", fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Ingresa un código promocional",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = LumiColors.Dark
                            )
                            Text(
                                "Canjea un código y recibe recompensas.",
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
                                placeholder = { Text("Código promocional") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            TextButton(
                                onClick = {
                                    promoMessage = if (promoCode.isBlank()) {
                                        "Escribe un código para continuar."
                                    } else {
                                        "El canje de códigos estará disponible próximamente."
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
                        offer.price?.let { append("\\n\\nPrecio: 💎 $it") }
                    },
                    color = LumiColors.Dark
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val price = offer.price
                        if (price != null && gems >= price) {
                            gems -= price
                            purchasedItems = purchasedItems + offer.title
                            if (offer.title == "Protector de racha" && equippedProtector < 2) {
                                equippedProtector++
                            }
                        }
                        selectedOffer = null
                    },
                    enabled = offer.price == null || gems >= offer.price
                ) {
                    Text(
                        if (offer.price == null) "Entendido" else "Canjear",
                        color = LumiColors.YellowStrong,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOffer = null }) {
                    Text("Cerrar", color = LumiColors.Dark)
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
                Text(offer.emoji, fontSize = 30.sp)
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
                Text(offer.emoji, fontSize = 31.sp)
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
                    offer.status ?: (offer.price?.let { "💎 $it" } ?: "DISPONIBLE"),
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
                Text(pack.emoji, fontSize = 30.sp)
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
