package eu.tutorials.shoppinglistapp

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

data class ShoppingItem(
    val id : Int,
    var name : String,
    var quantity : Int,
    var isEditing : Boolean = false,
    var address : String = ""
)


@Composable
fun ShoppingListApp(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context : Context,
    address : String
){

    var sItems by remember{ mutableStateOf(listOf<ShoppingItem>()) }
    var showDialog by remember{ mutableStateOf(false) }
    var itemName by remember{ mutableStateOf("") }
    var itemQty by remember{ mutableStateOf("")}


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions() ,
        onResult = { permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true){
                // I HAVE ACCESS to location

                locationUtils.requestLocationUpdates(viewModel = viewModel)
            }else{
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale( //a "rationale" is an explanation or justification provided to the user for why a particular permission is needed by the app.
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if(rationaleRequired){
                    Toast.makeText(context,
                        "Location Permission is required for this feature to work", Toast.LENGTH_LONG)
                        .show()
                }else{
                    Toast.makeText(context,
                        "Location Permission is required. Please enable it in the Android Settings",
                        Toast.LENGTH_LONG)
                        .show()
                }
            }
        })

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,

        ){
        Text(text = "Your Shopping List",modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(16.dp), fontSize = 32.sp, fontFamily = FontFamily.Serif)
        FilledTonalButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ){
            Text("Add Item")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            items(sItems){
                    item ->
                if(item.isEditing){
                    ShoppingItemEditor(item = item, onEditComplete = {
                            editedName , editedQty ->
                        sItems = sItems.map{it.copy(isEditing = false)} //here it represents each item of sItems that map keyword is iterating over
                        val editedItem = sItems.find{it.id == item.id}  //here it represents each item of sItems that find keyword is iterating over
                        editedItem?.let{
                            it.name = editedName
                            it.quantity = editedQty
                            it.address = address
                        }
                    })
                }else{
                    ShoppingListItem(item = item,
                        onEditClick = {
                            //finding out which item we are editing and changing it's "isEditing boolean" to true
                            sItems = sItems.map{it.copy(isEditing = it.id == item.id)} },
                        onDeleteClick = {
                            sItems = sItems-item
                        })
                }
                //* Refer to the screenshots for more understanding , location : C:\Users\hp\OneDrive\Pictures\Screenshots\Shopping List App
            }
        }
    }

    if(showDialog){
        AlertDialog(onDismissRequest = { showDialog = false },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Button(onClick = {
                        if(itemName.isNotBlank()){
                            val newItem = ShoppingItem(
                                id = sItems.size + 1,
                                name = itemName,
                                quantity = itemQty.toInt(),
                                address = address
                            )
                            sItems = sItems + newItem
                            showDialog = false
                            itemName = ""
                            itemQty = ""
                        }


                    }) {
                        Text("Add")
                    }
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
            title = {Text("Add Shopping Item")},
            text = {
                Column {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it } ,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = {Text("Enter Item")}
                    )

                    OutlinedTextField(
                        value = itemQty,
                        onValueChange = { itemQty = it } ,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = {Text(text = "Enter Quantity")}
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(onClick = {
                        if(locationUtils.hasLocationPermission(context)){
                            locationUtils.requestLocationUpdates(viewModel)
                            navController.navigate("locationscreen"){
                                this.launchSingleTop
                            }
                        }else{
                            requestPermissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        }
                    },modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Red)
                        Text(text = "Address")
                    }
                }
            }
        )
    }
}

@Composable
fun ShoppingItemEditor(item : ShoppingItem, onEditComplete : (String , Int) -> Unit){
    var editedName by remember{mutableStateOf(item.name)}
    var editedQty by remember {mutableStateOf(item.quantity.toString())}

    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Column {
            BasicTextField(
                value = editedName,
                onValueChange = {editedName = it},
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
            BasicTextField(
                value = editedQty,
                onValueChange = {editedQty = it},
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
        }
        Button(onClick = {
            onEditComplete(editedName , editedQty.toIntOrNull() ?: 1)
        }) {
            Text("Save")
        }
    }

}


@Composable
fun ShoppingListItem(
    item : ShoppingItem,
    onEditClick : () -> Unit,
    onDeleteClick : () -> Unit
){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                border = BorderStroke(2.dp, Color(0XFF018786)),
                shape = RoundedCornerShape(17)
            ),//**The functions in the modifier are applied sequentially
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp)){
                Text(text = "Item : ${item.name}" , modifier = Modifier.padding(8.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Qty: ${item.quantity}" , modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row (modifier = Modifier.fillMaxWidth()){
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Text(text = item.address)
            }

            Spacer(modifier = Modifier.height(6.dp))

            var checked by remember { mutableStateOf(false) }

            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(6.dp)
            ){

                Checkbox(checked = checked, onCheckedChange = {
                    checked = it
                })
                Text("Item bought")

            }


        }


        Row(modifier = Modifier.padding(8.dp)){
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}