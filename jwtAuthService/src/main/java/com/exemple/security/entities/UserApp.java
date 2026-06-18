package com.exemple.security.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "spots", "hikingSpots"})
@Table(name = "users")
public class UserApp implements Serializable, UserDetails {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String firstname;
	private String lastname;
	@Column(unique = true)
	private String email;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;
	
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Role> roles = new ArrayList<>();
	
	
	@OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<Spot> spots = new ArrayList<>();

	
	@OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<HikingSpot> hikingSpots = new ArrayList<>();

	// Spots favoris
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "user_favorite_spots",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "spot_id")
	)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private List<Spot> favoriteSpots = new ArrayList<>();

	// HikingSpots favoris
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "user_favorite_hiking_spots",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "hiking_spot_id")
	)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private List<HikingSpot> favoriteHikingSpots = new ArrayList<>();

	// Liste des amis (relation auto-référentielle)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "user_friends",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "friend_id")
	)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "friends", "favoriteSpots", "favoriteHikingSpots"})
	private List<UserApp> friends = new ArrayList<>();
	
	// Commentaires de l'utilisateur
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private List<Comment> comments = new ArrayList<>();
	
	// Appartenances aux groupes
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private List<GroupMembership> groupMemberships = new ArrayList<>();
	
	// Conversations (privées) auxquelles l'utilisateur participe
	@ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "participants", "messages"})
	private List<Conversation> conversations = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupInvitation> receivedInvitations = new ArrayList<>();

    @OneToMany(mappedBy = "invitedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupInvitation> sentInvitations = new ArrayList<>();
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		this.roles.forEach(r -> 
			authorities.add(new SimpleGrantedAuthority(r.toString()))
		);		
		
		return authorities;
	}
	
	@Override
	public String getUsername() {
		return email;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}