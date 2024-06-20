package com.university.geekway.adapters

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.R
import com.university.geekway.models.Comments
import com.university.geekway.databinding.RowCommentsBinding
import kotlin.math.roundToInt

class AdapterComment: RecyclerView.Adapter<AdapterComment.HolderComment> {

    val context: Context
    var commentArrayList: ArrayList<Comments>
    private lateinit var binding: RowCommentsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var isInMyNegativePlaces = false

    constructor(context: Context, commentArrayList: ArrayList<Comments>) {
        this.context = context
        this.commentArrayList = commentArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val model = commentArrayList[position]
        val comment = model.comment
        val rating = model.rating
        val date = model.date
        val uid = model.uid

        holder.commentTv.text = comment
        holder.ratingTv.text = rating
        holder.dateTv.text = date
        loadUserDetails(model, holder)

        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            } else {
                Toast.makeText(context, (R.string.text_notUIDComment), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCommentDialog(model: Comments, holder: HolderComment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.text_CommentDialog_delite)
            .setMessage(R.string.text_CommentDialog_deliteShure)
            .setPositiveButton(R.string.text_CommentDialog_deliteYes) {d,e ->
                val placeId = model.placeId
                val commentId = model.id
                val ref = FirebaseDatabase.getInstance().getReference("Places")
                ref.child(placeId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, (R.string.text_CommentDialog_deliteComplite), Toast.LENGTH_SHORT).show()
                        checkRating(model)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, (R.string.text_CommentDialog_deliteError), Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton(R.string.text_CommentDialog_deliteNo) {d,e ->
                d.dismiss()
            }
            .show()
    }

    private fun checkRating(model: Comments) {
        var rating = ""
        var placeId = ""
        val refCom = FirebaseDatabase.getInstance().getReference("Places")
        refCom.child(placeId).child("Comments").orderByChild("uid").equalTo(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    rating = "${snapshot.child("rating").value}"
                    placeId = "${snapshot.child("placeId").value}"
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        if (rating >= 3.0.toString()) {
            Log.e("TAG","Not Negative Place")
        }

        if (rating <= 2.5.toString()) {
            checkDeleteNegativePlaceComment(placeId)
        }

        submitRating(model)
    }

    private fun checkDeleteNegativePlaceComment(placeId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyNegativePlaces = snapshot.exists()
                    if (isInMyNegativePlaces) {
                        val refNeg = FirebaseDatabase.getInstance().getReference("Users")
                        refNeg.child(firebaseAuth.uid!!).child("Negatives").child(placeId)
                            .removeValue()
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                            }
                    } else {
                        Log.e("TAG","Not Negative Place")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun submitRating(model: Comments) {
        val placeId = model.placeId
        val db = FirebaseDatabase.getInstance().getReference("Places")
        val dbRef = db.child(placeId).child("Comments")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var total = 0.0
                var count = 0
                var placeRating = ""
                var placeComments = ""
                for (ds in dataSnapshot.children) {
                    val rating = ds.child("rating").value.toString().toDouble()
                    total = total + rating
                    count = count + 1
                    placeRating = (((total / count) * 100).roundToInt().toDouble() / 100).toString()
                    placeComments = ((placeComments.toIntOrNull()?:0) + 1).toString()
                    val refRat = FirebaseDatabase.getInstance().getReference("Places")
                    refRat.child(placeId).child("placeRating").setValue(placeRating)
                    val refCom = FirebaseDatabase.getInstance().getReference("Places")
                    refCom.child(placeId).child("placeComments").setValue(placeComments)
                }
                val refRat = FirebaseDatabase.getInstance().getReference("Places")
                refRat.child(placeId).child("placeRating").setValue(placeRating)
                val refCom = FirebaseDatabase.getInstance().getReference("Places")
                refCom.child(placeId).child("placeComments").setValue(placeComments)
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun loadUserDetails(model: Comments, holder: HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("Name").value}"
                    holder.nameTv.text = name
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    inner class HolderComment(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameTv = binding.nameTv
        val commentTv = binding.commentTv
        val ratingTv = binding.ratingTv
        val dateTv = binding.dateTv
    }
}