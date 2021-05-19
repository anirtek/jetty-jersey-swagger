=begin
#OpenAPI Petstore

#This is a sample server Petstore server. For this sample, you can use the api key `special-key` to test the authorization filters.

The version of the OpenAPI document: 1.0.0

Generated by: https://openapi-generator.tech
OpenAPI Generator version: 5.1.0

=end

require 'spec_helper'
require 'json'

# Unit tests for OpenapiClient::PetApi
# Automatically generated by openapi-generator (https://openapi-generator.tech)
# Please update as you see appropriate
describe 'PetApi' do
  before do
    # run before each test
    @api_instance = OpenapiClient::PetApi.new
  end

  after do
    # run after each test
  end

  describe 'test an instance of PetApi' do
    it 'should create an instance of PetApi' do
      expect(@api_instance).to be_instance_of(OpenapiClient::PetApi)
    end
  end

  # unit tests for add_pet
  # Add a new pet to the store
  # @param pet Pet object that needs to be added to the store
  # @param [Hash] opts the optional parameters
  # @return [Pet]
  describe 'add_pet test' do
    it 'should work' do
      # assertion here. ref: https://www.relishapp.com/rspec/rspec-expectations/docs/built-in-matchers
    end
  end

  # unit tests for delete_pet
  # Deletes a pet
  # @param pet_id Pet id to delete
  # @param [Hash] opts the optional parameters
  # @option opts [String] :api_key 
  # @return [nil]
  describe 'delete_pet test' do
    it 'should work' do
      # assertion here. ref: https://www.relishapp.com/rspec/rspec-expectations/docs/built-in-matchers
    end
  end

  # unit tests for find_pets_by_status
  # Finds Pets by status
  # Multiple status values can be provided with comma separated strings
  # @param status Status values that need to be considered for filter
  # @param [Hash] opts the optional parameters
  # @return [Array<Pet>]
  describe 'find_pets_by_status test' do
    it 'should work' do
      # assertion here. ref: https://www.relishapp.com/rspec/rspec-expectations/docs/built-in-matchers
    end
  end

  # unit tests for find_pets_by_tags
  # Finds Pets by tags
  # Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.
  # @param tags Tags to filter by
  # @param [Hash] opts the optional parameters
  # @return [Array<Pet>]
  describe 'find_pets_by_tags test' do
    it 'should work' do
      # assertion here. ref: https://www.relishapp.com/rspec/rspec-expectations/docs/built-in-matchers
    end
  end

  # unit tests for get_pet_by_id
  # Find pet by ID
  # Returns a single pet
  # @param pet_id ID of pet to return
  # @param [Hash] opts the optional parameters
  # @return [Pet]
  describe 'get_pet_by_id test' do
    it 'should work' do
      # assertion here. ref: https://www.relishapp.com/rspec/rspec-expectations/docs/built-in-matchers
    end
  end

  # unit tests for update_pet
  # Update an existing pet
  # @param pet Pet object that needs to be added to the store
  # @param [Hash] opts the optional parameters
  # @return [Pet]
  describe 'update_pet test' do
    it 'should work' do
      # assertion here. ref: https://www.relishapp.com/rspec/rspec-expectations/docs/built-in-matchers
    end
  end

  # unit tests for update_pet_with_form
  # Updates a pet in the store with form data
  # @param pet_id ID of pet that needs to be updated
  # @param [Hash] opts the optional parameters
  # @option opts [String] :name Updated name of the pet
  # @option opts [String] :status Updated status of the pet
  # @return [nil]
  describe 'update_pet_with_form test' do
    it 'should work' do
      # assertion here. ref: https://www.relishapp.com/rspec/rspec-expectations/docs/built-in-matchers
    end
  end

  # unit tests for upload_file
  # uploads an image
  # @param pet_id ID of pet to update
  # @param [Hash] opts the optional parameters
  # @option opts [String] :additional_metadata Additional data to pass to server
  # @option opts [File] :file file to upload
  # @return [ApiResponse]
  describe 'upload_file test' do
    it 'should work' do
      # assertion here. ref: https://www.relishapp.com/rspec/rspec-expectations/docs/built-in-matchers
    end
  end

end
