pragma solidity ^0.5.16;


/**
*  @dev Smart Contract resposible to notarize consents on the Ethereum Blockchain
*/
contract ConsentRegistry {

    struct ConsentGiven{
        address signer; // Notary
        uint date; // Date of notarization
        string hash; // Student Hash
        bool consent; // Parental consent
    }

    /**
     *  @dev Storage space used to record all consents notarized with metadata
     */
    mapping(bytes32 => ConsentGiven ) registry;

    /**
     *  @dev Notarize a consent identified by the hash of the consent hash, the sender and date in the registry
     *  @dev Emit an event Notarized in case of success
     *  @param _studentHash Consent hash
     */
    function setConsent(string calldata _studentHash, bool _consent) external returns (bool) {
        bytes32 id = keccak256(abi.encodePacked(_studentHash));

        registry[id].signer = msg.sender;
        registry[id].date = now;
        registry[id].hash = _studentHash;
        registry[id].consent = _consent;

        emit Notarized(msg.sender, _studentHash);

        return true;
    }

    /**
     *  @dev Verify a consent identified by its has was noterized in the registry previsouly.
     *  @param _studentHash Consent hash
     *  @return bool if consent was noterized previsouly in the registry
     */
    function getConsent(string calldata _studentHash) external view returns (bool) {
        return registry[keccak256(abi.encodePacked(_studentHash))].consent;
    }

    /**
     *  @dev Definition of the event triggered when a consent is successfully notarized in the registry
     */
    event Notarized(address indexed _signer, string _studentHash);
}

